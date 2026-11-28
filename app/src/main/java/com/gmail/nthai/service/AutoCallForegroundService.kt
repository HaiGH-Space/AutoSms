package com.gmail.nthai.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gmail.nthai.MainActivity
import com.gmail.nthai.R
import com.gmail.nthai.domain.engine.AutoCallEngine
import com.gmail.nthai.domain.model.AutoCallStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AutoCallForegroundService : Service() {

    @Inject
    lateinit var autoCallEngine: AutoCallEngine

    @Inject
    lateinit var callOverlayManager: com.gmail.nthai.domain.telephony.CallOverlayManager

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        observeEngineProgress()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                if ((autoCallEngine.progressState.value.status == AutoCallStatus.RUNNING) ||
                    (autoCallEngine.progressState.value.status == AutoCallStatus.PAUSED)) {
                    autoCallEngine.stopCampaign()
                }
                callOverlayManager.hideOverlay()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_PAUSE -> autoCallEngine.pauseCampaign()
            ACTION_RESUME -> autoCallEngine.resumeCampaign()
            ACTION_START -> {
                val notification = buildNotification(
                    content = "Đang chuẩn bị tự động gọi...",
                    current = 0,
                    total = 0,
                    isPaused = false
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        }
        return START_STICKY
    }

    private fun observeEngineProgress() {
        serviceScope.launch {
            autoCallEngine.progressState.collect { progress ->
                when (progress.status) {
                    AutoCallStatus.RUNNING -> {
                        val name = progress.currentCustomerName ?: progress.currentPhone ?: ""
                        val phone = progress.currentPhone ?: ""
                        val content = if (progress.isWaitingDelay) {
                            "Đang chờ ${progress.countdownRemainingSeconds}s để gọi tiếp..."
                        } else {
                            "Đang gọi: $name"
                        }
                        updateNotification(content, progress.completedCount, progress.totalCount, isPaused = false)

                        if (!progress.isWaitingDelay && name.isNotBlank() && phone.isNotBlank()) {
                            callOverlayManager.showOverlay(name, phone)
                        } else {
                            callOverlayManager.hideOverlay()
                        }
                    }
                    AutoCallStatus.PAUSED -> {
                        updateNotification("Tạm dừng gọi tự động", progress.completedCount, progress.totalCount, isPaused = true)
                        callOverlayManager.hideOverlay()
                    }
                    AutoCallStatus.FINISHED, AutoCallStatus.CANCELLED -> {
                        callOverlayManager.hideOverlay()
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                    AutoCallStatus.IDLE -> {
                        callOverlayManager.hideOverlay()
                    }
                }
            }
        }
    }

    private fun updateNotification(content: String, current: Int, total: Int, isPaused: Boolean) {
        val notification = buildNotification(content, current, total, isPaused)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(content: String, current: Int, total: Int, isPaused: Boolean): android.app.Notification {
        val mainIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AutoCallForegroundService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Auto Call Tiến trình")
            .setContentText("$content ($current/$total)")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dừng", stopPendingIntent)

        if (total > 0) {
            builder.setProgress(total, current, false)
        }

        if (isPaused) {
            val resumeIntent = Intent(this, AutoCallForegroundService::class.java).apply { action = ACTION_RESUME }
            val resumePendingIntent = PendingIntent.getService(
                this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_play, "Tiếp tục", resumePendingIntent)
        } else {
            val pauseIntent = Intent(this, AutoCallForegroundService::class.java).apply { action = ACTION_PAUSE }
            val pausePendingIntent = PendingIntent.getService(
                this, 3, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_pause, "Tạm dừng", pausePendingIntent)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tự động gọi điện (Auto Call)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Thông báo tiến trình tự động gọi điện hàng loạt"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callOverlayManager.hideOverlay()
        serviceJob.cancel()
    }

    companion object {
        const val CHANNEL_ID = "auto_call_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "com.gmail.nthai.ACTION_START_AUTO_CALL"
        const val ACTION_STOP = "com.gmail.nthai.ACTION_STOP_AUTO_CALL"
        const val ACTION_PAUSE = "com.gmail.nthai.ACTION_PAUSE_AUTO_CALL"
        const val ACTION_RESUME = "com.gmail.nthai.ACTION_RESUME_AUTO_CALL"

        fun startService(context: Context) {
            val intent = Intent(context, AutoCallForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AutoCallForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
