package com.gmail.nthai.domain.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import com.gmail.nthai.data.sms.SimManagerWrapper
import com.gmail.nthai.domain.model.AutoCallConfig
import com.gmail.nthai.domain.model.AutoCallProgress
import com.gmail.nthai.domain.model.AutoCallStatus
import com.gmail.nthai.domain.model.CallLogRecord
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.repository.AutoCallRepository
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
    @ApplicationContext private val context: Context,
    private val autoCallRepository: AutoCallRepository,
    private val callStateObserver: PhoneCallStateObserver,
    private val simManagerWrapper: SimManagerWrapper
) {
    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _progressState = MutableStateFlow(AutoCallProgress())
    val progressState: StateFlow<AutoCallProgress> = _progressState.asStateFlow()

    private var currentConfig = AutoCallConfig()
    private var customerQueue: List<Customer> = emptyList()
    private var currentIndex = 0

    private var executionJob: Job? = null
    private var callObserverJob: Job? = null
    private var isPaused = false

    fun startCampaign(config: AutoCallConfig, customers: List<Customer>) {
        executionJob?.cancel()
        executionJob = null
        callObserverJob?.cancel()
        callObserverJob = null
        callStateObserver.stopListening()

        if (customers.isEmpty()) {
            _progressState.update {
                it.copy(
                    status = AutoCallStatus.FINISHED,
                    errorMessage = "No customers selected for Auto Call."
                )
            }
            return
        }

        currentConfig = config
        customerQueue = customers
        currentIndex = 0
        isPaused = false

        val firstName = customers[0].customFields.values.firstOrNull { it.isNotBlank() } ?: customers[0].phone
        _progressState.value = AutoCallProgress(
            status = AutoCallStatus.RUNNING,
            totalCount = customers.size,
            completedCount = 0,
            currentCustomerName = firstName,
            currentPhone = customers[0].phone
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
                    status = if (currentIndex >= customerQueue.size && customerQueue.isNotEmpty())
                        AutoCallStatus.FINISHED else AutoCallStatus.CANCELLED,
                    isWaitingDelay = false,
                    countdownRemainingSeconds = 0
                )
            }
        }
    }

    private suspend fun runLoop() {
        while (currentIndex < customerQueue.size && _progressState.value.status == AutoCallStatus.RUNNING) {
            if (isPaused) break

            val customer = customerQueue[currentIndex]
            val customerDisplayName = customer.customFields.values.firstOrNull { it.isNotBlank() } ?: customer.phone
            _progressState.update {
                it.copy(
                    completedCount = currentIndex,
                    currentCustomerName = customerDisplayName,
                    currentPhone = customer.phone,
                    isWaitingDelay = false
                )
            }

            val startTime = System.currentTimeMillis()
            val logId = autoCallRepository.insertCallLog(
                CallLogRecord(
                    customerId = customer.id,
                    customerName = customerDisplayName,
                    phone = customer.phone,
                    timestamp = startTime,
                    status = "DIALING"
                )
            )

            // Trigger Phone Call
            val callLaunched = makePhoneCall(customer.phone)

            if (callLaunched) {
                // Wait until call finishes (OFFHOOK -> IDLE or timeout)
                waitForCallCompletion()

                val callDurationSec = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                autoCallRepository.updateCallLogStatus(
                    id = logId,
                    status = "COMPLETED",
                    durationSeconds = callDurationSec,
                    errorMessage = null
                )
            } else {
                autoCallRepository.updateCallLogStatus(
                    id = logId,
                    status = "FAILED",
                    durationSeconds = 0,
                    errorMessage = "Không thể khởi tạo cuộc gọi (Số ĐT không hợp lệ hoặc không có quyền)."
                )
            }

            currentIndex++
            _progressState.update { it.copy(completedCount = currentIndex) }

            if (currentIndex < customerQueue.size && !isPaused) {
                performDelay(currentConfig.delaySeconds)
            }
        }

        if (currentIndex >= customerQueue.size && customerQueue.isNotEmpty()) {
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

    private suspend fun waitForCallCompletion() {
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
