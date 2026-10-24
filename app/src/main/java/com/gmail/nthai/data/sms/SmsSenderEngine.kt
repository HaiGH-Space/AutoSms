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
        val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java).createForSubscriptionId(subscriptionId)
        } else {
            SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
        }

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
    }
}
