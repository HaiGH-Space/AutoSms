package com.gmail.nthai.domain.usecase

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.gmail.nthai.service.SmsStatusReceiver
import kotlinx.coroutines.delay
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessSimSendingUseCase @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val simConfigRepository: SimConfigRepository,
    private val sendingLogRepository: SendingLogRepository,
    private val parseSpintaxUseCase: ParseSpintaxUseCase,
    private val validateConstraintUseCase: ValidateSendingConstraintUseCase,
    private val calculateNextDelayUseCase: CalculateNextDelayUseCase,
    private val simManagerWrapper: SimManagerWrapper,
    private val smsSenderEngine: SmsSenderEngine
) {

    suspend fun recordFailedLogsForSim(
        simSlot: Int,
        customers: List<Customer>,
        existingLogsByCustomer: Map<Long, List<LogRecord>>,
        template: SmsTemplate?,
        errorMessage: String,
        sessionStartTime: Long = System.currentTimeMillis(),
        onReportSmsFailure: (Int, Long) -> Unit
    ) {
        for (customer in customers) {
            val recipients = customer.phone.split(',').map(String::trim).filter(String::isNotBlank)
            val currentLogs = sendingLogRepository.getLogsForCustomer(customer.id)

            for (recipient in recipients) {
                val existingLog = currentLogs
                    .filter { it.phone == recipient }
                    .maxByOrNull { it.timestamp }

                if (existingLog?.status == LogStatus.SENT_SUCCESS ||
                    (existingLog?.status == LogStatus.PENDING && existingLog.timestamp >= sessionStartTime)
                ) {
                    continue
                }

                val displayName = customer.customFields.values.firstOrNull { it.isNotBlank() } ?: recipient
                val content = if (template != null && template.content.isNotBlank()) {
                    parseSpintaxUseCase(template.content, customer.copy(phone = recipient))
                } else {
                    "Chưa chọn mẫu tin nhắn"
                }

                val logRecord = LogRecord(
                    id = existingLog?.takeIf { it.status == LogStatus.FAILED }?.id ?: 0,
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
                onReportSmsFailure(simSlot, logId)
            }

            val allCustomerLogs = sendingLogRepository.getLogsForCustomer(customer.id)
            val latestLogsByPhone = recipients.mapNotNull { phone ->
                allCustomerLogs.filter { it.phone == phone }.maxByOrNull { it.timestamp }
            }
            if (latestLogsByPhone.size >= recipients.size) {
                val hasFailed = latestLogsByPhone.any { it.status == LogStatus.FAILED }
                val newStatus = if (hasFailed) CustomerStatus.FAILED else CustomerStatus.SENT
                customerRepository.updateCustomerStatus(customer.id, newStatus)
            } else {
                customerRepository.updateCustomerStatus(customer.id, CustomerStatus.FAILED)
            }
        }
    }

    suspend fun execute(
        context: Context,
        simSlot: Int,
        customers: List<Customer>,
        existingLogsByCustomer: Map<Long, List<LogRecord>>,
        template: SmsTemplate,
        rule: SendingRule,
        isPaused: (Int) -> Boolean,
        isStopped: (Int) -> Boolean,
        sessionStartTime: Long = System.currentTimeMillis(),
        onProgressUpdate: (sent: Int, total: Int, currentCustomerName: String, nextDelaySec: Int?) -> Unit,
        onReportSmsFailure: (Int, Long) -> Unit
    ): String? {
        val currentSubId = simManagerWrapper.getSubscriptionIdForSlot(simSlot)
        if (currentSubId == null || currentSubId == -1) {
            val errorMsg = "Không tìm thấy SIM $simSlot đang hoạt động trên thiết bị."
            recordFailedLogsForSim(simSlot, customers, existingLogsByCustomer, template, errorMsg, sessionStartTime, onReportSmsFailure)
            return errorMsg
        }

        if (template.content.isBlank()) {
            val errorMsg = "Chưa chọn mẫu tin nhắn hoặc mẫu tin nhắn đang để trống."
            recordFailedLogsForSim(simSlot, customers, existingLogsByCustomer, template, errorMsg, sessionStartTime, onReportSmsFailure)
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

        var sent = 0
        val total = calculateTotal(customers, existingLogsByCustomer, sessionStartTime)

        worker@ for (customerIndex in customers.indices) {
            val customer = customers[customerIndex]
            if (isStopped(simSlot)) break

            val recipients = customer.phone.split(',').map(String::trim).filter(String::isNotBlank)

            for (recipient in recipients) {
                while (isPaused(simSlot) && !isStopped(simSlot)) {
                    delay(1000L)
                }
                if (isStopped(simSlot)) break@worker

                val currentLogs = sendingLogRepository.getLogsForCustomer(customer.id)
                val previousLog = currentLogs
                    .filter { it.phone == recipient }
                    .maxByOrNull { it.timestamp }
                if (previousLog?.status == LogStatus.SENT_SUCCESS ||
                    (previousLog?.status == LogStatus.PENDING && previousLog.timestamp >= sessionStartTime)
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
                        recordFailedLogsForSim(simSlot, remainingCustomers, existingLogsByCustomer, template, errorMsg, sessionStartTime, onReportSmsFailure)
                        return errorMsg
                    }
                    is ValidationResult.ExceededLimit -> {
                        val errorMsg = "SIM $simSlot đã đạt hạn mức gửi ${constraint.dailyLimit} tin/ngày."
                        val remainingCustomers = customers.subList(customerIndex, customers.size)
                        recordFailedLogsForSim(simSlot, remainingCustomers, existingLogsByCustomer, template, errorMsg, sessionStartTime, onReportSmsFailure)
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
                    context,
                    logId.toInt(),
                    Intent(context, SmsStatusReceiver::class.java).apply {
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
                    onReportSmsFailure(simSlot, logId)

                    val allCustomerLogs = sendingLogRepository.getLogsForCustomer(customer.id)
                    val latestLogsByPhone = recipients.mapNotNull { phone ->
                        allCustomerLogs.filter { it.phone == phone }.maxByOrNull { it.timestamp }
                    }
                    val finishedLogs = latestLogsByPhone.filter { it.status == LogStatus.SENT_SUCCESS || it.status == LogStatus.FAILED }

                    if (finishedLogs.size >= recipients.size) {
                        val hasFailed = finishedLogs.any { it.status == LogStatus.FAILED }
                        val newStatus = if (hasFailed) CustomerStatus.FAILED else CustomerStatus.SENT
                        customerRepository.updateCustomerStatus(customer.id, newStatus)
                    }
                }

                sent++
                onProgressUpdate(sent, total, displayName, null)

                if (sent < total) {
                    var secondsRemaining = (calculateNextDelayUseCase(
                        rule.minDelaySec,
                        rule.maxDelaySec
                    ) / 1000L).toInt()
                    while (secondsRemaining > 0) {
                        while (isPaused(simSlot) && !isStopped(simSlot)) {
                            delay(250L)
                        }
                        if (isStopped(simSlot)) break

                        onProgressUpdate(sent, total, displayName, secondsRemaining)
                        delay(1000L)
                        secondsRemaining--
                    }
                }
            }
        }
        return null
    }

    fun calculateTotal(
        customers: List<Customer>,
        existingLogsByCustomer: Map<Long, List<LogRecord>>,
        sessionStartTime: Long = 0L
    ): Int {
        return customers.sumOf { customer ->
            val existingLogs = existingLogsByCustomer[customer.id].orEmpty()
            customer.phone.split(',').map(String::trim).filter(String::isNotBlank).count { recipient ->
                val latestLog = existingLogs
                    .filter { it.phone == recipient }
                    .maxByOrNull { it.timestamp }
                val isPendingInCurrentSession = latestLog?.status == LogStatus.PENDING && latestLog.timestamp >= sessionStartTime
                latestLog?.status != LogStatus.SENT_SUCCESS && !isPendingInCurrentSession
            }
        }
    }
}
