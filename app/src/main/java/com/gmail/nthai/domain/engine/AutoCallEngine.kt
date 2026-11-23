package com.gmail.nthai.domain.engine

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat
import com.gmail.nthai.data.sms.SimManagerWrapper
import com.gmail.nthai.domain.model.AutoCallConfig
import com.gmail.nthai.domain.model.AutoCallProgress
import com.gmail.nthai.domain.model.AutoCallStatus
import com.gmail.nthai.domain.model.CallLogRecord
import com.gmail.nthai.domain.model.CallRecipient
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.repository.AutoCallRepository
import com.gmail.nthai.domain.repository.CallRecipientRepository
import com.gmail.nthai.domain.telephony.DeviceCallState
import com.gmail.nthai.domain.telephony.PhoneCallStateObserver
import com.gmail.nthai.service.AutoCallForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoCallEngine @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val autoCallRepository: AutoCallRepository,
    private val callRecipientRepository: CallRecipientRepository,
    private val callStateObserver: PhoneCallStateObserver,
    private val simManagerWrapper: SimManagerWrapper
) {
    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _progressState = MutableStateFlow(AutoCallProgress())
    val progressState: StateFlow<AutoCallProgress> = _progressState.asStateFlow()

    private var currentConfig = AutoCallConfig()
    private var recipientQueue: List<CallRecipient> = emptyList()
    private var currentIndex = 0

    private var executionJob: Job? = null
    private var callObserverJob: Job? = null
    private var isPaused = false

    fun startCampaign(config: AutoCallConfig, recipients: List<CallRecipient>) {
        executionJob?.cancel()
        executionJob = null
        callObserverJob?.cancel()
        callObserverJob = null
        callStateObserver.stopListening()

        if (recipients.isEmpty()) {
            _progressState.update {
                it.copy(
                    status = AutoCallStatus.FINISHED,
                    errorMessage = "Chưa có danh sách khách hàng để gọi tự động."
                )
            }
            return
        }

        currentConfig = config
        recipientQueue = recipients
        currentIndex = 0
        isPaused = false

        val firstName = recipients[0].name.ifBlank { recipients[0].phone }
        _progressState.value = AutoCallProgress(
            status = AutoCallStatus.RUNNING,
            totalCount = recipients.size,
            completedCount = 0,
            currentCustomerName = firstName,
            currentPhone = recipients[0].phone
        )

        // Start Foreground Service
        AutoCallForegroundService.startService(context)

        // Start Call State Observer
        callStateObserver.startListening()

        executionJob = engineScope.launch {
            runLoop()
        }
    }

    fun pauseCampaign() {
        if (_progressState.value.status == AutoCallStatus.RUNNING) {
            isPaused = true
            _progressState.update { it.copy(status = AutoCallStatus.PAUSED) }
        }
    }

    fun resumeCampaign() {
        if (_progressState.value.status == AutoCallStatus.PAUSED) {
            isPaused = false
            _progressState.update { it.copy(status = AutoCallStatus.RUNNING) }
            executionJob = engineScope.launch {
                runLoop()
            }
        }
    }

    fun stopCampaign() {
        val wasRunning = _progressState.value.status == AutoCallStatus.RUNNING || _progressState.value.status == AutoCallStatus.PAUSED

        executionJob?.cancel()
        executionJob = null
        callObserverJob?.cancel()
        callObserverJob = null

        callStateObserver.stopListening()
        AutoCallForegroundService.stopService(context)

        if (wasRunning) {
            _progressState.update {
                it.copy(
                    status = if (currentIndex >= recipientQueue.size && recipientQueue.isNotEmpty())
                        AutoCallStatus.FINISHED else AutoCallStatus.CANCELLED,
                    isWaitingDelay = false,
                    countdownRemainingSeconds = 0
                )
            }
        }
    }

    private suspend fun runLoop() {
        while (currentIndex < recipientQueue.size && _progressState.value.status == AutoCallStatus.RUNNING) {
            if (isPaused) break

            val recipient = recipientQueue[currentIndex]
            val recipientDisplayName = recipient.name.ifBlank { recipient.phone }
            _progressState.update {
                it.copy(
                    completedCount = currentIndex,
                    currentCustomerName = recipientDisplayName,
                    currentPhone = recipient.phone,
                    isWaitingDelay = false
                )
            }

            val startTime = System.currentTimeMillis()
            val logId = autoCallRepository.insertCallLog(
                CallLogRecord(
                    customerId = recipient.id,
                    customerName = recipientDisplayName,
                    phone = recipient.phone,
                    timestamp = startTime,
                    status = "DIALING"
                )
            )

            // Trigger Phone Call
            val callLaunched = makePhoneCall(recipient.phone)

            if (callLaunched) {
                // Wait until call finishes (OFFHOOK -> IDLE or timeout)
                val hasBeenOffhook = waitForCallCompletion()

                // Allow system CallLog provider time to write the record
                delay(800)

                val sysLog = queryLatestCallLog(recipient.phone, startTime)

                if (sysLog != null) {
                    if (sysLog.isAnswered) {
                        autoCallRepository.updateCallLogStatus(
                            id = logId,
                            status = "COMPLETED",
                            durationSeconds = sysLog.durationSeconds,
                            errorMessage = null
                        )
                        callRecipientRepository.updateRecipientStatus(
                            id = recipient.id,
                            status = LogStatus.SENT_SUCCESS,
                            durationSeconds = sysLog.durationSeconds,
                            note = sysLog.noteText
                        )
                    } else {
                        autoCallRepository.updateCallLogStatus(
                            id = logId,
                            status = sysLog.statusText,
                            durationSeconds = 0,
                            errorMessage = sysLog.noteText
                        )
                        callRecipientRepository.updateRecipientStatus(
                            id = recipient.id,
                            status = LogStatus.FAILED,
                            durationSeconds = 0,
                            note = sysLog.noteText
                        )
                    }
                } else {
                    if (hasBeenOffhook) {
                        val callDurationSec = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                        autoCallRepository.updateCallLogStatus(
                            id = logId,
                            status = "COMPLETED",
                            durationSeconds = callDurationSec,
                            errorMessage = null
                        )
                        callRecipientRepository.updateRecipientStatus(
                            id = recipient.id,
                            status = LogStatus.SENT_SUCCESS,
                            durationSeconds = callDurationSec,
                            note = "Đã nghe máy ($callDurationSec s)"
                        )
                    } else {
                        autoCallRepository.updateCallLogStatus(
                            id = logId,
                            status = "NO_ANSWER",
                            durationSeconds = 0,
                            errorMessage = "Không nghe máy (0s)"
                        )
                        callRecipientRepository.updateRecipientStatus(
                            id = recipient.id,
                            status = LogStatus.FAILED,
                            durationSeconds = 0,
                            note = "Không nghe máy (0s)"
                        )
                    }
                }
            } else {
                val err = "Không thể khởi tạo cuộc gọi (Số ĐT không hợp lệ hoặc không có quyền)."
                autoCallRepository.updateCallLogStatus(
                    id = logId,
                    status = "FAILED",
                    durationSeconds = 0,
                    errorMessage = err
                )
                callRecipientRepository.updateRecipientStatus(
                    id = recipient.id,
                    status = LogStatus.FAILED,
                    durationSeconds = 0,
                    note = err
                )
            }

            currentIndex++
            _progressState.update { it.copy(completedCount = currentIndex) }

            if (currentIndex < recipientQueue.size && !isPaused) {
                performDelay(currentConfig.delaySeconds)
            }
        }

        if (currentIndex >= recipientQueue.size && recipientQueue.isNotEmpty()) {
            _progressState.update {
                it.copy(
                    status = AutoCallStatus.FINISHED,
                    currentCustomerName = null,
                    currentPhone = null,
                    isWaitingDelay = false
                )
            }
            callStateObserver.stopListening()
            AutoCallForegroundService.stopService(context)
        }
    }

    private fun makePhoneCall(phone: String): Boolean {
        val cleanedPhone = phone.replace(Regex("[^0-9+]"), "")
        if (cleanedPhone.isBlank()) {
            return false
        }

        val slotIndex = (currentConfig.simSlot - 1).coerceAtLeast(0)
        val subscriptionId = simManagerWrapper.getSubscriptionIdForSlot(currentConfig.simSlot)

        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$cleanedPhone")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK

            getPhoneAccountHandle(subscriptionId, slotIndex)?.let { handle ->
                putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", handle)
            }

            putExtra("simSlot", slotIndex)
            putExtra("com.android.phone.extra.slot", slotIndex)
            putExtra("slot", slotIndex)
            putExtra("sim_slot", slotIndex)

            subscriptionId?.let { subId ->
                putExtra("subscription", subId)
                putExtra("sub_id", subId)
                putExtra("com.android.phone.force.slot", true)
            }
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getPhoneAccountHandle(subId: Int?, simSlotIndex: Int): PhoneAccountHandle? {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager ?: return null
        return try {
            @Suppress("MissingPermission")
            val accounts = telecomManager.callCapablePhoneAccounts ?: return null
            if (subId != null) {
                accounts.firstOrNull { it.id.contains(subId.toString()) }
                    ?: if (simSlotIndex in accounts.indices) accounts[simSlotIndex] else accounts.firstOrNull()
            } else {
                if (simSlotIndex in accounts.indices) accounts[simSlotIndex] else accounts.firstOrNull()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun waitForCallCompletion(): Boolean {
        var hasBeenOffhook = false
        var isCallActive = true

        val maxWaitMs = 120_000L
        val maxDialWaitMs = 10_000L // Give up to 10 seconds for call to start / dial / connect
        val startMs = System.currentTimeMillis()

        callObserverJob = engineScope.launch {
            callStateObserver.callStateFlow.collect { state ->
                when (state) {
                    DeviceCallState.OFFHOOK, DeviceCallState.RINGING -> {
                        hasBeenOffhook = true
                    }
                    DeviceCallState.IDLE -> {
                        if (hasBeenOffhook) {
                            isCallActive = false
                        }
                    }
                }
            }
        }

        // Wait until call becomes active (OFFHOOK/RINGING) or dial timeout is reached
        val dialDeadline = startMs + maxDialWaitMs
        while (!hasBeenOffhook && System.currentTimeMillis() < dialDeadline) {
            if (isPaused || _progressState.value.status != AutoCallStatus.RUNNING) break
            if (callStateObserver.currentState == DeviceCallState.IDLE && (System.currentTimeMillis() - startMs) > 2_000L) {
                break
            }
            delay(300)
        }

        // If call went offhook/ringing, wait until it returns to IDLE or timeout
        if (hasBeenOffhook) {
            while (isCallActive && (System.currentTimeMillis() - startMs) < maxWaitMs) {
                if (isPaused || _progressState.value.status != AutoCallStatus.RUNNING) break
                if (callStateObserver.currentState == DeviceCallState.IDLE) {
                    break
                }
                delay(500)
            }
        }

        callObserverJob?.cancel()
        callObserverJob = null
        return hasBeenOffhook
    }

    private data class CallResultDetail(
        val isAnswered: Boolean,
        val durationSeconds: Int,
        val statusText: String,
        val noteText: String
    )

    private fun queryLatestCallLog(phone: String, callStartTimeMs: Long): CallResultDetail? {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        val cleanTargetPhone = phone.replace(Regex("[^0-9]"), "")
        if (cleanTargetPhone.isBlank()) return null

        val uri = CallLog.Calls.CONTENT_URI
        val selection = "${CallLog.Calls.DATE} >= ?"
        val selectionArgs = arrayOf((callStartTimeMs - 10000).toString())
        val sortOrder = "${CallLog.Calls.DATE} DESC"

        try {
            context.contentResolver.query(uri, null, selection, selectionArgs, sortOrder)?.use { cursor ->
                val numberIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val durationIdx = cursor.getColumnIndex(CallLog.Calls.DURATION)
                val typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE)
                val causeIdx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cursor.getColumnIndex("disconnect_cause")
                } else -1

                while (cursor.moveToNext()) {
                    val number = if (numberIdx >= 0) cursor.getString(numberIdx) ?: "" else ""
                    val cleanNumber = number.replace(Regex("[^0-9]"), "")

                    val isMatch = cleanNumber == cleanTargetPhone ||
                            (cleanTargetPhone.length >= 8 && cleanNumber.endsWith(cleanTargetPhone.takeLast(8))) ||
                            (cleanNumber.length >= 8 && cleanTargetPhone.endsWith(cleanNumber.takeLast(8)))

                    if (isMatch) {
                        val duration = if (durationIdx >= 0) cursor.getInt(durationIdx) else 0
                        val type = if (typeIdx >= 0) cursor.getInt(typeIdx) else 0
                        val cause = if (causeIdx >= 0) cursor.getInt(causeIdx) else -1

                        if (duration > 0) {
                            return CallResultDetail(
                                isAnswered = true,
                                durationSeconds = duration,
                                statusText = "COMPLETED",
                                noteText = "Đã nghe máy (${duration}s)"
                            )
                        } else {
                            val (statusText, noteText) = when {
                                cause == 7 || cause == 1 -> "NO_NUMBER" to "Số không tồn tại / Không có thực"
                                cause == 4 -> "BUSY" to "Máy bận"
                                cause == 5 -> "REJECTED" to "Cuộc gọi bị từ chối"
                                cause == 8 || cause == 11 -> "UNREACHABLE" to "Thuê bao / Không liên lạc được"
                                type == CallLog.Calls.BLOCKED_TYPE -> "BLOCKED" to "Cuộc gọi bị chặn"
                                else -> "NO_ANSWER" to "Không nghe máy (0s)"
                            }

                            return CallResultDetail(
                                isAnswered = false,
                                durationSeconds = 0,
                                statusText = statusText,
                                noteText = noteText
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }



    private suspend fun performDelay(seconds: Int) {
        _progressState.update {
            it.copy(
                isWaitingDelay = true,
                countdownRemainingSeconds = seconds
            )
        }

        for (remaining in seconds downTo 1) {
            if (isPaused || _progressState.value.status != AutoCallStatus.RUNNING) break
            _progressState.update { it.copy(countdownRemainingSeconds = remaining) }
            delay(1000)
        }

        _progressState.update {
            it.copy(
                isWaitingDelay = false,
                countdownRemainingSeconds = 0
            )
        }
    }
}
