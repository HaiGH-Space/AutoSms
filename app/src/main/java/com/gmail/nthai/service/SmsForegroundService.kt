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
import com.gmail.nthai.domain.engine.SmsSendingEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    @Inject
    lateinit var smsSendingEngine: SmsSendingEngine

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instanceEngine = smsSendingEngine
        createNotificationChannel()
        observeEngineState()
    }

    private fun observeEngineState() {
        serviceScope.launch {
            smsSendingEngine.serviceStates.collect { states ->
                val (sent, total) = smsSendingEngine.getAggregateProgress()
                val activeSlot = states.entries.find { it.value is SendingServiceState.Running }?.key
                val pausedSlot = states.entries.find { it.value is SendingServiceState.Paused }?.key

                val title = when {
                    activeSlot != null -> {
                        val runningState = states[activeSlot] as? SendingServiceState.Running
                        val name = runningState?.currentCustomerName.orEmpty()
                        if (name.isNotBlank()) "Đang gửi SIM $activeSlot: $name" else "Đang gửi SIM $activeSlot"
                    }
                    pausedSlot != null -> "Đang tạm dừng SIM $pausedSlot"
                    else -> "Đang xử lý tiến trình gửi SMS"
                }

                if (smsSendingEngine.isAnyJobActive()) {
                    updateNotification(title, sent, total, isPaused = (pausedSlot != null && activeSlot == null))
                } else if (states.values.all { it is SendingServiceState.Idle || it is SendingServiceState.Finished || it is SendingServiceState.Error }) {
                    stopServiceIfIdle()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        val simSlot = intent?.getIntExtra(EXTRA_SIM_SLOT, 0) ?: 0
        when (action) {
            ACTION_START -> {
                try {
                    val (sent, total) = smsSendingEngine.getAggregateProgress()
                    startForegroundWithNotification("Đang chuẩn bị gửi tin...", sent, total)
                } catch (e: Exception) {
                    android.util.Log.e("SmsForegroundService", "Failed to start foreground service", e)
                    stopSelf()
                    return START_NOT_STICKY
                }
                smsSendingEngine.startSending(simSlot)
            }

            ACTION_PAUSE -> {
                smsSendingEngine.pauseSlot(simSlot)
            }

            ACTION_RESUME -> {
                smsSendingEngine.resumeSlot(simSlot)
            }

            ACTION_STOP -> {
                smsSendingEngine.stopSlot(simSlot)
            }
        }
        return START_STICKY
    }

    private fun stopServiceIfIdle() {
        if (!smsSendingEngine.isAnyJobActive()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
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

    @SuppressLint("InlinedApi")
    private fun startForegroundWithNotification(title: String, progress: Int, maxProgress: Int) {
        val notification = buildNotification(title, progress, maxProgress, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
    private fun updateNotification(
        title: String,
        progress: Int,
        maxProgress: Int,
        isPaused: Boolean = false
    ) {
        val notification = buildNotification(title, progress, maxProgress, isPaused)
        val manager = getSystemService(NotificationManager::class.java)
        try {
            manager?.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission missing
        }
    }

    private fun buildNotification(
        title: String,
        progress: Int,
        maxProgress: Int,
        isPaused: Boolean
    ): android.app.Notification {
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
                this,
                1,
                resumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(0, "Tiếp tục", resumePendingIntent).build()
        } else {
            val pauseIntent = Intent(this, SmsForegroundService::class.java).apply {
                action = ACTION_PAUSE
                putExtra(EXTRA_SIM_SLOT, 0)
            }
            val pausePendingIntent = PendingIntent.getService(
                this,
                2,
                pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
        if (instanceEngine == smsSendingEngine) {
            instanceEngine = null
        }
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

        @Volatile
        private var instanceEngine: SmsSendingEngine? = null

        fun reportSmsFailure(simSlot: Int, logId: Long) {
            instanceEngine?.reportSmsFailure(simSlot, logId)
        }

        private val fallbackStates = MutableStateFlow<Map<Int, SendingServiceState>>(
            mapOf(1 to SendingServiceState.Idle, 2 to SendingServiceState.Idle)
        )

        val serviceStates: StateFlow<Map<Int, SendingServiceState>>
            get() = instanceEngine?.serviceStates ?: fallbackStates.asStateFlow()
    }
}
