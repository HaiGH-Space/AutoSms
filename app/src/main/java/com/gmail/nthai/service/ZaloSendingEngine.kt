package com.gmail.nthai.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.model.ZaloRecipient
import com.gmail.nthai.domain.repository.ZaloRepository
import com.gmail.nthai.domain.usecase.ParseSpintaxUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

data class ZaloSendingProgressState(
    val isSending: Boolean = false,
    val isPaused: Boolean = false,
    val sentCount: Int = 0,
    val totalCount: Int = 0,
    val currentRecipient: ZaloRecipient? = null,
    val countdownSeconds: Int = 0,
    val statusMessage: String = "Sẵn sàng"
)

@Singleton
class ZaloSendingEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val zaloRepository: ZaloRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val parseSpintaxUseCase = ParseSpintaxUseCase()

    private val _progressState = MutableStateFlow(ZaloSendingProgressState())
    val progressState: StateFlow<ZaloSendingProgressState> = _progressState.asStateFlow()

    private var campaignJob: Job? = null

    fun startCampaign(
        messageTemplate: String,
        delayMinSec: Int,
        delayMaxSec: Int,
        autoAddFriend: Boolean,
        sendOnlyIfUnsent: Boolean
    ) {
        if (_progressState.value.isSending) return

        campaignJob = scope.launch {
            val pendingRecipients = zaloRepository.getPendingRecipients()
            if (pendingRecipients.isEmpty()) {
                _progressState.update {
                    it.copy(
                        isSending = false,
                        statusMessage = "Không có số điện thoại chờ gửi Zalo"
                    )
                }
                return@launch
            }

            _progressState.update {
                it.copy(
                    isSending = true,
                    isPaused = false,
                    sentCount = 0,
                    totalCount = pendingRecipients.size,
                    statusMessage = "Đang bắt đầu chiến dịch gửi Zalo..."
                )
            }

            for ((index, recipient) in pendingRecipients.withIndex()) {
                if (!_progressState.value.isSending) break

                while (_progressState.value.isPaused) {
                    delay(500)
                    if (!_progressState.value.isSending) break
                }
                if (!_progressState.value.isSending) break

                _progressState.update {
                    it.copy(
                        currentRecipient = recipient,
                        statusMessage = "Đang mở Zalo gửi tới: ${recipient.name} (${recipient.phone})"
                    )
                }

                val dummyCustomer = Customer(
                    id = recipient.id,
                    phone = recipient.phone,
                    status = com.gmail.nthai.domain.model.CustomerStatus.PENDING,
                    customFields = mapOf("tên" to recipient.name)
                )
                val messageText = parseSpintaxUseCase(messageTemplate, dummyCustomer)

                // 1. Prepare ZaloAccessibilityService
                ZaloAccessibilityService.prepareAutoSend(messageText, autoAddFriend)

                // 2. Launch Zalo Deep Link
                val cleanPhone = recipient.phone.replace(Regex("[^0-9+]"), "")
                val zaloUri = Uri.parse("https://zalo.me/$cleanPhone")
                val intent = Intent(Intent.ACTION_VIEW, zaloUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to launch Zalo deep link for $cleanPhone", e)
                }

                // 3. Wait for send result from ZaloAccessibilityService or timeout
                val sendResult = withTimeoutOrNull(6000L) {
                    ZaloAccessibilityService.sendEvents.firstOrNull()
                }

                val isSuccess = sendResult is ZaloAccessibilityService.SendResult.Success
                val isNotFound = sendResult is ZaloAccessibilityService.SendResult.NotFound
                val statusStr = if (isSuccess) "SENT_SUCCESS" else "FAILED"
                val noteStr = when {
                    isSuccess -> "Đã gửi qua Trợ năng Zalo"
                    isNotFound -> "Số điện thoại chưa đăng ký Zalo hoặc không hợp lệ"
                    else -> "Không thể tự động gửi Zalo"
                }

                zaloRepository.updateRecipientStatus(recipient.id, statusStr, noteStr)
                zaloRepository.addLog(
                    LogRecord(
                        customerId = recipient.id,
                        customerName = recipient.name,
                        phone = recipient.phone,
                        messageContent = messageText,
                        simSlot = 0,
                        status = if (isSuccess) LogStatus.SENT_SUCCESS else LogStatus.FAILED,
                        errorMessage = if (!isSuccess) noteStr else null
                    )
                )

                // Always bring app back to front after processing recipient
                try {
                    val bringIntent = Intent(context, com.gmail.nthai.MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    }
                    context.startActivity(bringIntent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to bring app to front", e)
                }

                ZaloAccessibilityService.clearPendingAction()

                val updatedSentCount = index + 1
                _progressState.update {
                    it.copy(sentCount = updatedSentCount)
                }

                // 4. Random Anti-Spam delay interval
                if (index < pendingRecipients.size - 1 && _progressState.value.isSending) {
                    val delaySec = if (delayMaxSec > delayMinSec) {
                        Random.nextInt(delayMinSec, delayMaxSec + 1)
                    } else {
                        delayMinSec
                    }

                    for (sec in delaySec downTo 1) {
                        if (!_progressState.value.isSending) break
                        while (_progressState.value.isPaused) {
                            delay(500)
                            if (!_progressState.value.isSending) break
                        }
                        if (!_progressState.value.isSending) break

                        _progressState.update {
                            it.copy(
                                countdownSeconds = sec,
                                statusMessage = "Đang đếm ngược giãn cách $sec giây chống spam Zalo..."
                            )
                        }
                        delay(1000)
                    }
                }
            }

            _progressState.update {
                it.copy(
                    isSending = false,
                    isPaused = false,
                    currentRecipient = null,
                    countdownSeconds = 0,
                    statusMessage = "Chiến dịch gửi Zalo đã hoàn thành!"
                )
            }
        }
    }

    fun pauseCampaign() {
        _progressState.update { it.copy(isPaused = true, statusMessage = "Đã tạm dừng gửi Zalo") }
    }

    fun resumeCampaign() {
        _progressState.update {
            it.copy(
                isPaused = false,
                statusMessage = "Đang tiếp tục gửi Zalo..."
            )
        }
    }

    fun stopCampaign() {
        campaignJob?.cancel()
        ZaloAccessibilityService.clearPendingAction()
        _progressState.update {
            it.copy(
                isSending = false,
                isPaused = false,
                currentRecipient = null,
                countdownSeconds = 0,
                statusMessage = "Đã dừng chiến dịch gửi Zalo"
            )
        }
    }

    companion object {
        private const val TAG = "ZaloSendingEngine"
    }
}
