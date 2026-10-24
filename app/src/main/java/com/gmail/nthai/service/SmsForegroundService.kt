package com.gmail.nthai.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gmail.nthai.R
import com.gmail.nthai.data.sms.SimManagerWrapper
import com.gmail.nthai.data.sms.SmsSenderEngine
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.model.SimConfig
import com.gmail.nthai.domain.model.SmsTemplate
import com.gmail.nthai.domain.repository.CustomerRepository
import com.gmail.nthai.domain.repository.SendingLogRepository
import com.gmail.nthai.domain.repository.SimConfigRepository
import com.gmail.nthai.domain.repository.TemplateRepository
import com.gmail.nthai.domain.usecase.CalculateNextDelayUseCase
import com.gmail.nthai.domain.usecase.ParseSpintaxUseCase
import com.gmail.nthai.domain.usecase.ValidateSendingConstraintUseCase
import com.gmail.nthai.domain.usecase.ValidationResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class SendingServiceState {
    data object Idle : SendingServiceState()
    data class Running(val sentCount: Int, val totalCount: Int, val currentCustomerName: String) : SendingServiceState()
    data class Paused(val sentCount: Int, val totalCount: Int) : SendingServiceState()
    data class Finished(val sentCount: Int, val totalCount: Int) : SendingServiceState()
    data class Error(val message: String) : SendingServiceState()
}

@AndroidEntryPoint
class SmsForegroundService : Service() {

    @Inject lateinit var customerRepository: CustomerRepository
    @Inject lateinit var templateRepository: TemplateRepository
    @Inject lateinit var simConfigRepository: SimConfigRepository
    @Inject lateinit var sendingLogRepository: SendingLogRepository
    @Inject lateinit var parseSpintaxUseCase: ParseSpintaxUseCase
    @Inject lateinit var validateConstraintUseCase: ValidateSendingConstraintUseCase
    @Inject lateinit var calculateNextDelayUseCase: CalculateNextDelayUseCase
    @Inject lateinit var simManagerWrapper: SimManagerWrapper
    @Inject lateinit var smsSenderEngine: SmsSenderEngine

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var sendingJob: Job? = null

    private var isPaused = false
    private var isStopped = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        when (action) {
            ACTION_START -> {
                isStopped = false
                isPaused = false
                startForegroundWithNotification("Đang chuẩn bị gửi tin...", 0, 0)
                startSendingProcess()
            }
            ACTION_PAUSE -> {
                isPaused = true
                updateState(SendingServiceState.Paused(currentSent, currentTotal))
                updateNotification("Tạm dừng gửi SMS", currentSent, currentTotal, isPaused = true)
            }
            ACTION_RESUME -> {
                isPaused = false
            }
            ACTION_STOP -> {
                stopSendingProcess()
            }
        }
        return START_STICKY
    }

    private var currentSent = 0
    private var currentTotal = 0

    private fun startSendingProcess() {
        sendingJob?.cancel()
        sendingJob = serviceScope.launch {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            simConfigRepository.resetDailyCountsIfNewDay(todayDate)

            val pendingCustomers = customerRepository.getPendingCustomers().first()
            if (pendingCustomers.isEmpty()) {
                updateState(SendingServiceState.Finished(0, 0))
                updateNotification("Không có tin nhắn cần gửi", 0, 0)
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf()
                return@launch
            }

            currentTotal = pendingCustomers.sumOf { customer ->
                customer.phone.split(',').count { it.isNotBlank() }
            }
            currentSent = 0

            val template = templateRepository.getTemplate().first() ?: SmsTemplate()
            val rule = simConfigRepository.getSendingRule().first()

            for (customer in pendingCustomers) {
                if (isStopped) break

                val recipients = customer.phone.split(',').map(String::trim).filter(String::isNotBlank)
                val existingLogs = sendingLogRepository.getLogsForCustomer(customer.id)

                for (recipient in recipients) {
                    while (isPaused && !isStopped) {
                        delay(1000L)
                    }

                    if (isStopped) break

                    val isAlreadySent = existingLogs.any {
                        it.phone == recipient && (it.status == LogStatus.SENT_SUCCESS || it.status == LogStatus.FAILED)
                    }
                    if (isAlreadySent) {
                        currentSent++
                        continue
                    }

                    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    var activeSimSlot = 0
                    var chosenSubId = -1

                    for (slot in 1..2) {
                        var config = simConfigRepository.getSimConfigBySlot(slot)
                        val currentSubId = simManagerWrapper.getSubscriptionIdForSlot(slot)
                            ?: config?.subscriptionId
                            ?: -1

                        if (currentSubId != -1) {
                            if (config == null) {
                                config = SimConfig(subscriptionId = currentSubId, simSlot = slot, dailyLimit = 100)
                                simConfigRepository.saveSimConfig(config)
                            } else if (config.subscriptionId != currentSubId) {
                                config = config.copy(subscriptionId = currentSubId)
                                simConfigRepository.saveSimConfig(config)
                            }

                            val constraint = validateConstraintUseCase(rule, currentHour, config)
                            if (constraint is ValidationResult.OutsideTimeFrame) {
                                val msg = "Ngoài khung giờ làm việc (${rule.startHour}h - ${rule.endHour}h)"
                                updateState(SendingServiceState.Error(msg))
                                updateNotification(msg, currentSent, currentTotal)
                                return@launch
                            }

                            if (constraint is ValidationResult.Valid) {
                                activeSimSlot = slot
                                chosenSubId = currentSubId
                                break
                            }
                        }
                    }

                    if (chosenSubId == -1) {
                        val msg = "Không tìm thấy SIM hoạt động hoặc tất cả SIM đã đạt hạn mức gửi trong ngày"
                        updateState(SendingServiceState.Error(msg))
                        updateNotification(msg, currentSent, currentTotal)
                        return@launch
                    }

                    val updatedCustomFields = customer.customFields.toMutableMap()
                    updatedCustomFields.keys.filter { key ->
                        key.equals("sdt", ignoreCase = true) ||
                        key.equals("sđt", ignoreCase = true) ||
                        key.equals("phone", ignoreCase = true) ||
                        key.equals(rule.phoneColumnName, ignoreCase = true)
                    }.forEach { key ->
                        updatedCustomFields[key] = recipient
                    }

                    val recipientCustomer = customer.copy(
                        phone = recipient,
                        customFields = updatedCustomFields
                    )
                    val messageContent = parseSpintaxUseCase(template.content, recipientCustomer)
                    val displayName = customer.customFields.values.firstOrNull { it.isNotBlank() } ?: recipient

                    val logId = sendingLogRepository.insertLog(
                        LogRecord(
                            customerId = customer.id,
                            customerName = displayName,
                            phone = recipient,
                            messageContent = messageContent,
                            simSlot = activeSimSlot,
                            status = LogStatus.PENDING
                        )
                    )

                    val sentPendingIntent = PendingIntent.getBroadcast(
                        this@SmsForegroundService,
                        logId.toInt(),
                        Intent(this@SmsForegroundService, SmsStatusReceiver::class.java).apply {
                            action = SmsStatusReceiver.ACTION_SMS_SENT
                            putExtra(SmsStatusReceiver.EXTRA_LOG_ID, logId)
                            putExtra(SmsStatusReceiver.EXTRA_CUSTOMER_ID, customer.id)
                            putExtra(SmsStatusReceiver.EXTRA_SIM_SLOT, activeSimSlot)
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    smsSenderEngine.sendSms(
                        subscriptionId = chosenSubId,
                        destinationAddress = recipient,
                        text = messageContent,
                        sentIntent = sentPendingIntent,
                        deliveryIntent = null
                    )

                    currentSent++
                    updateState(SendingServiceState.Running(currentSent, currentTotal, displayName))
                    updateNotification("Đang gửi $displayName", currentSent, currentTotal)

                    val delayMs = calculateNextDelayUseCase(rule.minDelaySec, rule.maxDelaySec)
                    delay(delayMs)
                }

                if (isStopped) break
            }

            if (!isStopped) {
                updateState(SendingServiceState.Finished(currentSent, currentTotal))
                updateNotification("Hoàn thành gửi $currentSent/$currentTotal tin nhắn", currentSent, currentTotal)
            }
        }
    }

    private fun stopSendingProcess() {
        isStopped = true
        sendingJob?.cancel()
        updateState(SendingServiceState.Idle)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateState(newState: SendingServiceState) {
        _serviceState.value = newState
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tiến trình gửi SMS tự động",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Hiển thị tiến độ phát tin nhắn SMS ngầm"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundWithNotification(title: String, progress: Int, maxProgress: Int) {
        val notification = buildNotification(title, progress, maxProgress, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(title: String, progress: Int, maxProgress: Int, isPaused: Boolean = false) {
        val notification = buildNotification(title, progress, maxProgress, isPaused)
        val manager = getSystemService(NotificationManager::class.java)
        try {
            manager?.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission missing
        }
    }

    private fun buildNotification(title: String, progress: Int, maxProgress: Int, isPaused: Boolean): android.app.Notification {
        val stopIntent = Intent(this, SmsForegroundService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeAction = if (isPaused) {
            val resumeIntent = Intent(this, SmsForegroundService::class.java).apply { action = ACTION_RESUME }
            val resumePendingIntent = PendingIntent.getService(
                this, 1, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(0, "Tiếp tục", resumePendingIntent).build()
        } else {
            val pauseIntent = Intent(this, SmsForegroundService::class.java).apply { action = ACTION_PAUSE }
            val pausePendingIntent = PendingIntent.getService(
                this, 2, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(0, "Tạm dừng", pausePendingIntent).build()
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AutoSms - $title")
            .setContentText("Đã gửi: $progress/$maxProgress tin")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(maxProgress.coerceAtLeast(1), progress, maxProgress == 0)
            .setOngoing(true)
            .addAction(pauseResumeAction)
            .addAction(NotificationCompat.Action.Builder(0, "Hủy", stopPendingIntent).build())
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    companion object {
        const val CHANNEL_ID = "auto_sms_foreground_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.gmail.nthai.ACTION_START"
        const val ACTION_PAUSE = "com.gmail.nthai.ACTION_PAUSE"
        const val ACTION_RESUME = "com.gmail.nthai.ACTION_RESUME"
        const val ACTION_STOP = "com.gmail.nthai.ACTION_STOP"

        private val _serviceState = MutableStateFlow<SendingServiceState>(SendingServiceState.Idle)
        val serviceState: StateFlow<SendingServiceState> = _serviceState.asStateFlow()
    }
}
