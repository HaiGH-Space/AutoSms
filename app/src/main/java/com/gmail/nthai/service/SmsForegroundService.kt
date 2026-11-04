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
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.model.SendingRule
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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

sealed class SendingServiceState {
    data object Idle : SendingServiceState()
    data class Running(
        val sentCount: Int,
        val totalCount: Int,
        val currentCustomerName: String,
        val secondsUntilNext: Int? = null,
        val failedCount: Int = 0
    ) : SendingServiceState()
    data class Paused(
        val sentCount: Int,
        val totalCount: Int,
        val failedCount: Int = 0
    ) : SendingServiceState()
    data class Finished(
        val sentCount: Int,
        val totalCount: Int,
        val failedCount: Int = 0
    ) : SendingServiceState()
    data class Error(
        val message: String,
        val sentCount: Int = 0,
        val totalCount: Int = 0,
        val failedCount: Int = 0
    ) : SendingServiceState()
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
    private val sendingJobs = ConcurrentHashMap<Int, Job>()
    private val pausedSlots = ConcurrentHashMap.newKeySet<Int>()
    private val stoppedSlots = ConcurrentHashMap.newKeySet<Int>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        val simSlot = intent?.getIntExtra(EXTRA_SIM_SLOT, 0) ?: 0
        when (action) {
            ACTION_START -> {
                startForegroundWithNotification("Đang chuẩn bị gửi tin...", 0, 0)
                if (simSlot in 1..2) {
                    startSendingProcess(simSlot)
                } else {
                    (1..2).forEach { startSendingProcess(it) }
                }
            }
            ACTION_PAUSE -> {
                controlSlots(simSlot) { slot -> pauseSlot(slot) }
            }
            ACTION_RESUME -> {
                controlSlots(simSlot) { slot -> resumeSlot(slot) }
            }
            ACTION_STOP -> {
                controlSlots(simSlot) { slot -> stopSlot(slot) }
            }
        }
        return START_STICKY
    }

    private val sentCounts = mutableMapOf(1 to 0, 2 to 0)
    private val totalCounts = mutableMapOf(1 to 0, 2 to 0)
    private val progressLock = Any()

    private fun controlSlots(requestedSlot: Int, action: (Int) -> Unit) {
        if (requestedSlot in 1..2) action(requestedSlot) else (1..2).forEach(action)
    }

    private fun pauseSlot(slot: Int) {
        if (sendingJobs[slot]?.isActive == true) {
            pausedSlots.add(slot)
            val counts = synchronized(progressLock) {
                (sentCounts[slot] ?: 0) to (totalCounts[slot] ?: 0)
            }
            updateSimState(
                slot,
                SendingServiceState.Paused(counts.first, counts.second, currentFailedCount(slot))
            )
            updateAggregateNotification("Đang tạm dừng SIM $slot")
        }
    }

    private fun resumeSlot(slot: Int) {
        if (sendingJobs[slot]?.isActive == true && pausedSlots.remove(slot)) {
            val counts = synchronized(progressLock) {
                (sentCounts[slot] ?: 0) to (totalCounts[slot] ?: 0)
            }
            updateSimState(
                slot,
                SendingServiceState.Running(
                    counts.first,
                    counts.second,
                    "",
                    failedCount = currentFailedCount(slot)
                )
            )
            updateAggregateNotification("Đang tiếp tục SIM $slot")
        }
    }

    private fun stopSlot(slot: Int) {
        stoppedSlots.add(slot)
        pausedSlots.remove(slot)
        sendingJobs[slot]?.cancel()
        updateSimState(slot, SendingServiceState.Idle)
        stopServiceIfIdle()
    }

    private fun startSendingProcess(simSlot: Int) {
        if (sendingJobs[simSlot]?.isActive == true) return
        stoppedSlots.remove(simSlot)
        pausedSlots.remove(simSlot)
        synchronized(progressLock) {
            sentCounts[simSlot] = 0
            totalCounts[simSlot] = 0
        }
        synchronized(failureCountLock) {
            failedLogIdsBySlot[simSlot] = mutableSetOf()
        }
        updateSimState(simSlot, SendingServiceState.Running(0, 0, "Đang chuẩn bị"))
        val job = serviceScope.launch(start = CoroutineStart.LAZY) {
          try {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            simConfigRepository.resetDailyCountsIfNewDay(todayDate)

            val pendingCustomers = customerRepository.getSelectedCustomersForSending().first()
                .filter { it.sendSimSlot == simSlot }
            if (pendingCustomers.isEmpty()) {
                updateSimState(simSlot, SendingServiceState.Finished(0, 0))
                return@launch
            }

            val existingLogsByCustomer = pendingCustomers.associate { customer ->
                customer.id to sendingLogRepository.getLogsForCustomer(customer.id)
            }
            val currentTotal = pendingCustomers.sumOf { customer ->
                val existingLogs = existingLogsByCustomer[customer.id].orEmpty()
                customer.phone.split(',').map(String::trim).filter(String::isNotBlank).count { recipient ->
                    val latestLog = existingLogs
                        .filter { it.phone == recipient }
                        .maxByOrNull { it.timestamp }
                    latestLog?.status != LogStatus.PENDING &&
                            latestLog?.status != LogStatus.SENT_SUCCESS
                }
            }
            synchronized(progressLock) {
                totalCounts[simSlot] = currentTotal
                sentCounts[simSlot] = 0
            }

            val template = templateRepository.getSelectedTemplate().first()
                ?: templateRepository.getAllTemplates().first().firstOrNull()
                ?: SmsTemplate()
            val rule = simConfigRepository.getSendingRule().first()
            val error = sendCustomersForSim(
                simSlot = simSlot,
                customers = pendingCustomers,
                existingLogsByCustomer = existingLogsByCustomer,
                template = template,
                rule = rule
            )
            if (!stoppedSlots.contains(simSlot)) {
                val counts = synchronized(progressLock) {
                    (sentCounts[simSlot] ?: 0) to (totalCounts[simSlot] ?: 0)
                }
                val state = error?.let {
                    SendingServiceState.Error(it, counts.first, counts.second, currentFailedCount(simSlot))
                }
                    ?: SendingServiceState.Finished(counts.first, counts.second, currentFailedCount(simSlot))
                updateSimState(simSlot, state)
            }
          } catch (e: Exception) {
            if (!stoppedSlots.contains(simSlot)) {
                val errorMsg = e.message ?: "Lỗi hệ thống khi gửi SMS bằng SIM $simSlot."
                try {
                    val pendingCustomers = customerRepository.getSelectedCustomersForSending().first()
                        .filter { it.sendSimSlot == simSlot }
                    if (pendingCustomers.isNotEmpty()) {
                        val existingLogsByCustomer = pendingCustomers.associate { customer ->
                            customer.id to sendingLogRepository.getLogsForCustomer(customer.id)
                        }
                        recordFailedLogsForSim(simSlot, pendingCustomers, existingLogsByCustomer, null, errorMsg)
                    }
                } catch (_: Exception) {}

                val counts = synchronized(progressLock) {
                    (sentCounts[simSlot] ?: 0) to (totalCounts[simSlot] ?: 0)
                }
                updateSimState(
                    simSlot,
                    SendingServiceState.Error(
                        errorMsg,
                        counts.first,
                        counts.second,
                        currentFailedCount(simSlot)
                    )
                )
            }
          } finally {
            sendingJobs.remove(simSlot)
            stopServiceIfIdle()
          }
        }
        sendingJobs[simSlot] = job
        job.start()
    }

    private suspend fun recordFailedLogsForSim(
        simSlot: Int,
        customers: List<Customer>,
        existingLogsByCustomer: Map<Long, List<LogRecord>>,
        template: SmsTemplate?,
        errorMessage: String
    ) {
        for (customer in customers) {
            val recipients = customer.phone.split(',').map(String::trim).filter(String::isNotBlank)
            val existingLogs = existingLogsByCustomer[customer.id].orEmpty()

            for (recipient in recipients) {
                val previousLog = existingLogs
                    .filter { it.phone == recipient }
                    .maxByOrNull { it.timestamp }

                if (previousLog?.status == LogStatus.SENT_SUCCESS) {
                    continue
                }

                val displayName = customer.customFields.values.firstOrNull { it.isNotBlank() } ?: recipient
                val content = if (template != null && template.content.isNotBlank()) {
                    parseSpintaxUseCase(template.content, customer.copy(phone = recipient))
                } else {
                    "Chưa chọn mẫu tin nhắn"
                }

                val logRecord = LogRecord(
                    id = previousLog?.takeIf { it.status == LogStatus.FAILED }?.id ?: 0,
                    customerId = customer.id,
                    customerName = displayName,
                    phone = recipient,
                    messageContent = content,
                    simSlot = simSlot,
                    timestamp = System.currentTimeMillis(),
                    status = LogStatus.FAILED,
                    errorMessage = errorMessage
                )

                val logId = if (logRecord.id != 0L) {
                    sendingLogRepository.updateLog(logRecord)
                    logRecord.id
                } else {
                    sendingLogRepository.insertLog(logRecord)
                }
                reportSmsFailure(simSlot, logId)
            }
            customerRepository.updateCustomerStatus(customer.id, CustomerStatus.FAILED)
        }
    }

    private suspend fun sendCustomersForSim(
        simSlot: Int,
        customers: List<Customer>,
        existingLogsByCustomer: Map<Long, List<LogRecord>>,
        template: SmsTemplate,
        rule: SendingRule,
    ): String? {
        val currentSubId = simManagerWrapper.getSubscriptionIdForSlot(simSlot)
        if (currentSubId == null || currentSubId == -1) {
            val errorMsg = "Không tìm thấy SIM $simSlot đang hoạt động trên thiết bị."
            recordFailedLogsForSim(simSlot, customers, existingLogsByCustomer, template, errorMsg)
            return errorMsg
        }

        if (template.content.isBlank()) {
            val errorMsg = "Chưa chọn mẫu tin nhắn hoặc mẫu tin nhắn đang để trống."
            recordFailedLogsForSim(simSlot, customers, existingLogsByCustomer, template, errorMsg)
            return errorMsg
        }

        var config = simConfigRepository.getSimConfigBySlot(simSlot)
        val carrier = simManagerWrapper.getActiveSims().find { it.simSlot == simSlot }?.carrierName.orEmpty()
        val defaultCarrierLimit = SimConfig.getDefaultDailyLimit(carrier)

        if (config == null) {
            config = SimConfig(
                subscriptionId = currentSubId,
                simSlot = simSlot,
                carrierName = carrier,
                dailyLimit = defaultCarrierLimit
            )
            simConfigRepository.saveSimConfig(config)
        } else {
            val isSimSwapped = config.subscriptionId != -1 && config.subscriptionId != currentSubId
            var updatedConfig = config
            var needsUpdate = false

            if (isSimSwapped) {
                updatedConfig = updatedConfig.copy(
                    subscriptionId = currentSubId,
                    carrierName = carrier,
                    dailyLimit = defaultCarrierLimit,
                    sentCountToday = 0
                )
                needsUpdate = true
            } else {
                if (config.subscriptionId != currentSubId) {
                    updatedConfig = updatedConfig.copy(subscriptionId = currentSubId)
                    needsUpdate = true
                }
                if (carrier.isNotBlank() && updatedConfig.carrierName != carrier) {
                    updatedConfig = updatedConfig.copy(carrierName = carrier)
                    needsUpdate = true
                }
                if (updatedConfig.dailyLimit == 100 && defaultCarrierLimit != 100) {
                    updatedConfig = updatedConfig.copy(dailyLimit = defaultCarrierLimit)
                    needsUpdate = true
                }
            }
            if (needsUpdate) {
                config = updatedConfig
                simConfigRepository.saveSimConfig(config)
            }
        }

        worker@ for (customerIndex in customers.indices) {
            val customer = customers[customerIndex]
            if (stoppedSlots.contains(simSlot)) break

            val recipients = customer.phone.split(',').map(String::trim).filter(String::isNotBlank)
            val existingLogs = existingLogsByCustomer[customer.id].orEmpty()

            for (recipient in recipients) {
                while (pausedSlots.contains(simSlot) && !stoppedSlots.contains(simSlot)) {
                    delay(1000L)
                }
                if (stoppedSlots.contains(simSlot)) break@worker

                val previousLog = existingLogs
                    .filter { it.phone == recipient }
                    .maxByOrNull { it.timestamp }
                if (previousLog?.status == LogStatus.PENDING ||
                    previousLog?.status == LogStatus.SENT_SUCCESS
                ) {
                    continue
                }

                val currentConfig = simConfigRepository.getSimConfigBySlot(simSlot) ?: config
                val currentTime = Calendar.getInstance()
                when (val constraint = validateConstraintUseCase(
                    rule,
                    currentTime.get(Calendar.HOUR_OF_DAY),
                    currentConfig,
                    currentMinute = currentTime.get(Calendar.MINUTE)
                )) {
                    is ValidationResult.OutsideTimeFrame -> {
                        val errorMsg = "Ngoài khung giờ làm việc (${rule.startHour.toString().padStart(2, '0')}:${rule.startMinute.toString().padStart(2, '0')} - ${rule.endHour.toString().padStart(2, '0')}:${rule.endMinute.toString().padStart(2, '0')})."
                        val remainingCustomers = customers.subList(customerIndex, customers.size)
                        recordFailedLogsForSim(simSlot, remainingCustomers, existingLogsByCustomer, template, errorMsg)
                        return errorMsg
                    }
                    is ValidationResult.ExceededLimit -> {
                        val errorMsg = "SIM $simSlot đã đạt hạn mức gửi ${constraint.dailyLimit} tin/ngày."
                        val remainingCustomers = customers.subList(customerIndex, customers.size)
                        recordFailedLogsForSim(simSlot, remainingCustomers, existingLogsByCustomer, template, errorMsg)
                        return errorMsg
                    }
                    ValidationResult.Valid -> Unit
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

                val recipientCustomer = customer.copy(phone = recipient, customFields = updatedCustomFields)
                val messageContent = parseSpintaxUseCase(template.content, recipientCustomer)
                val displayName = customer.customFields.values.firstOrNull { it.isNotBlank() } ?: recipient
                val logRecord = LogRecord(
                    id = previousLog?.takeIf { it.status == LogStatus.FAILED }?.id ?: 0,
                    customerId = customer.id,
                    customerName = displayName,
                    phone = recipient,
                    messageContent = messageContent,
                    simSlot = simSlot,
                    timestamp = System.currentTimeMillis(),
                    status = LogStatus.PENDING
                )
                val logId = if (logRecord.id != 0L) {
                    sendingLogRepository.updateLog(logRecord)
                    logRecord.id
                } else {
                    sendingLogRepository.insertLog(logRecord)
                }

                val sentPendingIntent = PendingIntent.getBroadcast(
                    this@SmsForegroundService,
                    logId.toInt(),
                    Intent(this@SmsForegroundService, SmsStatusReceiver::class.java).apply {
                        action = SmsStatusReceiver.ACTION_SMS_SENT
                        putExtra(SmsStatusReceiver.EXTRA_LOG_ID, logId)
                        putExtra(SmsStatusReceiver.EXTRA_CUSTOMER_ID, customer.id)
                        putExtra(SmsStatusReceiver.EXTRA_SIM_SLOT, simSlot)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    smsSenderEngine.sendSms(
                        subscriptionId = currentSubId,
                        destinationAddress = recipient,
                        text = messageContent,
                        sentIntent = sentPendingIntent,
                        deliveryIntent = null
                    )
                } catch (e: Exception) {
                    val errorMsg = e.message ?: "Lỗi khởi tạo gửi SMS tới $recipient"
                    sendingLogRepository.updateLogStatus(logId, LogStatus.FAILED, errorMsg)
                    reportSmsFailure(simSlot, logId)

                    val customerLogs = sendingLogRepository.getLogsForCustomer(customer.id)
                    val totalNumbers = customer.phone.split(',').count { it.isNotBlank() }
                    val finishedLogs = customerLogs.filter { it.status == LogStatus.SENT_SUCCESS || it.status == LogStatus.FAILED }

                    if (finishedLogs.size >= totalNumbers) {
                        val hasFailed = finishedLogs.any { it.status == LogStatus.FAILED }
                        val newStatus = if (hasFailed) CustomerStatus.FAILED else CustomerStatus.SENT
                        customerRepository.updateCustomerStatus(customer.id, newStatus)
                    }
                }

                val (sent, total) = synchronized(progressLock) {
                    val sent = (sentCounts[simSlot] ?: 0) + 1
                    val total = totalCounts[simSlot] ?: 0
                    sentCounts[simSlot] = sent
                    sent to total
                }
                if (sent >= total) {
                    updateSimState(
                        simSlot,
                        SendingServiceState.Finished(sent, total, currentFailedCount(simSlot))
                    )
                } else {
                    updateSimState(
                        simSlot,
                        SendingServiceState.Running(
                            sent,
                            total,
                            displayName,
                            failedCount = currentFailedCount(simSlot)
                        )
                    )
                }
                updateAggregateNotification("Đang gửi SIM $simSlot: $displayName")
                if (sent < total) {
                    var secondsRemaining = (calculateNextDelayUseCase(
                        rule.minDelaySec,
                        rule.maxDelaySec
                    ) / 1000L).toInt()
                    while (secondsRemaining > 0) {
                        while (pausedSlots.contains(simSlot) && !stoppedSlots.contains(simSlot)) {
                            delay(250L)
                        }
                        if (stoppedSlots.contains(simSlot)) break

                        updateSimState(
                            simSlot,
                            SendingServiceState.Running(
                                sent,
                                total,
                                displayName,
                                secondsRemaining,
                                currentFailedCount(simSlot)
                            )
                        )
                        delay(1000L)
                        secondsRemaining--
                    }
                }
            }
        }
        return null
    }

    private fun stopServiceIfIdle() {
        if (sendingJobs.values.none { it.isActive }) {
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private fun updateSimState(simSlot: Int, newState: SendingServiceState) {
        _serviceStates.update { it + (simSlot to newState) }
    }

    private fun currentFailedCount(simSlot: Int): Int = synchronized(failureCountLock) {
        failedLogIdsBySlot[simSlot]?.size ?: 0
    }

    private fun updateAggregateNotification(title: String) {
        val (sent, total) = synchronized(progressLock) {
            sentCounts.values.sum() to totalCounts.values.sum()
        }
        updateNotification(title, sent, total)
    }

    private fun createNotificationChannel() {
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
        val stopIntent = Intent(this, SmsForegroundService::class.java).apply {
            action = ACTION_STOP
            putExtra(EXTRA_SIM_SLOT, 0)
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeAction = if (isPaused) {
            val resumeIntent = Intent(this, SmsForegroundService::class.java).apply {
                action = ACTION_RESUME
                putExtra(EXTRA_SIM_SLOT, 0)
            }
            val resumePendingIntent = PendingIntent.getService(
                this, 1, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(0, "Tiếp tục", resumePendingIntent).build()
        } else {
            val pauseIntent = Intent(this, SmsForegroundService::class.java).apply {
                action = ACTION_PAUSE
                putExtra(EXTRA_SIM_SLOT, 0)
            }
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
        const val EXTRA_SIM_SLOT = "com.gmail.nthai.EXTRA_SIM_SLOT"

        fun reportSmsFailure(simSlot: Int, logId: Long) {
            val failedCount = synchronized(failureCountLock) {
                val failedLogIds = failedLogIdsBySlot.getOrPut(simSlot) { mutableSetOf() }
                failedLogIds.add(logId)
                failedLogIds.size
            }
            _serviceStates.update { states ->
                val updatedState = when (val state = states[simSlot]) {
                    is SendingServiceState.Running -> state.copy(failedCount = failedCount)
                    is SendingServiceState.Paused -> state.copy(failedCount = failedCount)
                    is SendingServiceState.Finished -> state.copy(failedCount = failedCount)
                    is SendingServiceState.Error -> state.copy(failedCount = failedCount)
                    SendingServiceState.Idle, null -> return@update states
                }
                states + (simSlot to updatedState)
            }
        }

        private val failureCountLock = Any()
        private val failedLogIdsBySlot = mutableMapOf(1 to mutableSetOf<Long>(), 2 to mutableSetOf<Long>())

        private val _serviceStates = MutableStateFlow<Map<Int, SendingServiceState>>(
            mapOf(1 to SendingServiceState.Idle, 2 to SendingServiceState.Idle)
        )
        val serviceStates: StateFlow<Map<Int, SendingServiceState>> = _serviceStates.asStateFlow()
    }
}
