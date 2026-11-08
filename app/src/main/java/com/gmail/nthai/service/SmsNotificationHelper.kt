package com.gmail.nthai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.gmail.nthai.MainActivity
import com.gmail.nthai.R

class SmsNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "SmsForegroundServiceChannel"
        const val NOTIFICATION_ID = 1001
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Tự động gửi SMS",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Thông báo tiến trình tự động phát tin SMS"
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(
        statusText: String,
        sentCount: Int = 0,
        totalCount: Int = 0,
        isPaused: Boolean = false
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(context, SmsForegroundService::class.java).apply {
            action = if (isPaused) SmsForegroundService.ACTION_RESUME else SmsForegroundService.ACTION_PAUSE
        }
        val pausePendingIntent = PendingIntent.getService(
            context,
            1,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, SmsForegroundService::class.java).apply {
            action = SmsForegroundService.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("AutoSms - Đang tự động phát tin")
            .setContentText(statusText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        if (totalCount > 0) {
            builder.setProgress(totalCount, sentCount, false)
        } else {
            builder.setProgress(0, 0, true)
        }

        if (isPaused) {
            builder.addAction(R.mipmap.ic_launcher, "Tiếp tục", pausePendingIntent)
        } else {
            builder.addAction(R.mipmap.ic_launcher, "Tạm dừng", pausePendingIntent)
        }
        builder.addAction(R.mipmap.ic_launcher, "Hủy tất cả", stopPendingIntent)

        return builder.build()
    }

    fun updateNotification(
        statusText: String,
        sentCount: Int = 0,
        totalCount: Int = 0,
        isPaused: Boolean = false
    ) {
        val notification = buildNotification(statusText, sentCount, totalCount, isPaused)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
