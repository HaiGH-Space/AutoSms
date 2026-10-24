package com.gmail.nthai.service

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.gmail.nthai.domain.model.CustomerStatus
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.repository.CustomerRepository
import com.gmail.nthai.domain.repository.SendingLogRepository
import com.gmail.nthai.domain.repository.SimConfigRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsStatusReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sendingLogRepository: SendingLogRepository

    @Inject
    lateinit var customerRepository: CustomerRepository

    @Inject
    lateinit var simConfigRepository: SimConfigRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        val action = intent.action ?: return
        val logId = intent.getLongExtra(EXTRA_LOG_ID, -1L)
        val customerId = intent.getLongExtra(EXTRA_CUSTOMER_ID, -1L)
        val simSlot = intent.getIntExtra(EXTRA_SIM_SLOT, 1)

        if (logId == -1L || customerId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (action == ACTION_SMS_SENT) {
                    val resultCode = pendingResult.resultCode
                    if (resultCode == Activity.RESULT_OK) {
                        sendingLogRepository.updateLogStatus(logId, LogStatus.SENT_SUCCESS)
                        simConfigRepository.incrementSentCount(simSlot)
                    } else {
                        val errorMsg = when (resultCode) {
                            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "Lỗi chung hệ thống (Generic Failure)"
                            SmsManager.RESULT_ERROR_NO_SERVICE -> "Không có sóng di động (No Service)"
                            SmsManager.RESULT_ERROR_NULL_PDU -> "PDU tin nhắn rỗng"
                            SmsManager.RESULT_ERROR_RADIO_OFF -> "Chế độ máy bay / Tắt SIM"
                            else -> "Lỗi gửi SMS (Mã: $resultCode)"
                        }
                        sendingLogRepository.updateLogStatus(logId, LogStatus.FAILED, errorMsg)
                    }

                    val customer = customerRepository.getCustomerById(customerId)
                    if (customer != null) {
                        val totalNumbers = customer.phone.split(',').count { it.isNotBlank() }
                        val customerLogs = sendingLogRepository.getLogsForCustomer(customerId)
                        val finishedLogs = customerLogs.filter { it.status == LogStatus.SENT_SUCCESS || it.status == LogStatus.FAILED }

                        if (finishedLogs.size >= totalNumbers) {
                            val hasFailed = finishedLogs.any { it.status == LogStatus.FAILED }
                            val newStatus = if (hasFailed) CustomerStatus.FAILED else CustomerStatus.SENT
                            customerRepository.updateCustomerStatus(customerId, newStatus)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SMS_SENT = "com.gmail.nthai.ACTION_SMS_SENT"
        const val ACTION_SMS_DELIVERED = "com.gmail.nthai.ACTION_SMS_DELIVERED"
        const val EXTRA_LOG_ID = "extra_log_id"
        const val EXTRA_CUSTOMER_ID = "extra_customer_id"
        const val EXTRA_SIM_SLOT = "extra_sim_slot"
    }
}
