package com.gmail.nthai.data.sms

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.telephony.SmsManager

class SmsSenderEngine(
    private val context: Context
) {

    @Suppress("DEPRECATION")
    fun sendSms(
        subscriptionId: Int,
        destinationAddress: String,
        text: String,
        sentIntent: PendingIntent?,
        deliveryIntent: PendingIntent?
    ) {
        val smsManager: SmsManager = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java).createForSubscriptionId(subscriptionId)
            } else {
                SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
            }
        } catch (e: Exception) {
            throw IllegalStateException("Không thể khởi tạo dịch vụ SIM (SubID: $subscriptionId): ${e.localizedMessage ?: e.message}", e)
        }

        try {
            val parts = smsManager.divideMessage(text)
            if (parts.size > 1) {
                val sentIntents = ArrayList<PendingIntent?>().apply {
                    repeat(parts.size) { add(sentIntent) }
                }
                val deliveryIntents = ArrayList<PendingIntent?>().apply {
                    repeat(parts.size) { add(deliveryIntent) }
                }
                smsManager.sendMultipartTextMessage(
                    destinationAddress,
                    null,
                    parts,
                    sentIntents,
                    deliveryIntents
                )
            } else {
                smsManager.sendTextMessage(
                    destinationAddress,
                    null,
                    text,
                    sentIntent,
                    deliveryIntent
                )
            }
        } catch (e: SecurityException) {
            throw SecurityException("Ứng dụng chưa được cấp quyền gửi SMS (SEND_SMS).", e)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Số điện thoại ($destinationAddress) hoặc nội dung tin nhắn không hợp lệ.", e)
        } catch (e: Exception) {
            throw Exception("Lỗi hệ thống khi khởi tạo gửi SMS: ${e.localizedMessage ?: e.message ?: e.javaClass.simpleName}", e)
        }
    }
}
