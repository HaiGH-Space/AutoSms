package com.gmail.nthai.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.data.excel.ExcelParser
import com.gmail.nthai.data.local.dao.ZaloRecipientStatsEntity
import com.gmail.nthai.data.local.mapper.toEntity
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.ZaloRecipient
import com.gmail.nthai.domain.repository.ZaloRepository
import com.gmail.nthai.service.ZaloAccessibilityService
import com.gmail.nthai.service.ZaloSendingEngine
import com.gmail.nthai.service.ZaloSendingProgressState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class ZaloViewModel @Inject constructor(
    private val zaloRepository: ZaloRepository,
    private val zaloSendingEngine: ZaloSendingEngine
) : ViewModel() {

    val recipients: StateFlow<List<ZaloRecipient>> = zaloRepository.observeRecipients()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val stats: StateFlow<ZaloRecipientStatsEntity> = zaloRepository.observeStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ZaloRecipientStatsEntity(0, 0, 0, 0)
        )

    val logs: StateFlow<List<LogRecord>> = zaloRepository.observeLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val progressState: StateFlow<ZaloSendingProgressState> = zaloSendingEngine.progressState

    // Config state
    val messageContent = MutableStateFlow(
        "{Xin chào|Chào} {ten_khach}, bên em đang có chương trình ưu đãi tri ân khách hàng. Vui lòng nhắn tin lại để nhận mã giảm giá nhé!"
    )
    val delayRange = MutableStateFlow(10f..25f)
    val autoAddFriend = MutableStateFlow(true)
    val sendOnlyIfUnsent = MutableStateFlow(true)

    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

    private val excelParser = ExcelParser()

    fun checkAccessibilityStatus(context: Context) {
        val enabled = ZaloAccessibilityService.isAccessibilityEnabled(context)
        _isAccessibilityEnabled.value = enabled
    }

    fun addRecipient(name: String, phone: String) {
        if (phone.isBlank()) return
        viewModelScope.launch {
            val recipient = ZaloRecipient(
                name = name.ifBlank { "Khách hàng Mới" },
                phone = phone.trim()
            )
            zaloRepository.insertRecipient(recipient)
        }
    }

    fun deleteRecipient(id: Long) {
        viewModelScope.launch {
            zaloRepository.deleteRecipient(id)
        }
    }

    fun resetRecipientStatus(id: Long) {
        viewModelScope.launch {
            zaloRepository.resetRecipientStatus(id)
        }
    }

    fun resetAllStatus() {
        viewModelScope.launch {
            zaloRepository.resetAllRecipientStatus()
        }
    }

    fun clearRecipients() {
        viewModelScope.launch {
            zaloRepository.deleteAllRecipients()
        }
    }

    fun importExcelFile(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
                val parseResult = excelParser.parseCustomers(inputStream)
                parseResult.onSuccess { parsedCustomers ->
                    val newRecipients = parsedCustomers.map { customer ->
                        val name = customer.customFields.entries.find { (k, _) ->
                            k.contains("tên", ignoreCase = true) || k.contains(
                                "ten",
                                ignoreCase = true
                            )
                        }?.value ?: "Khách hàng"

                        ZaloRecipient(
                            name = name,
                            phone = customer.phone,
                            customFieldsJson = customer.toEntity().customFieldsJson
                        )
                    }
                    if (newRecipients.isNotEmpty()) {
                        zaloRepository.insertRecipients(newRecipients)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startCampaign() {
        val minDelay = delayRange.value.start.toInt()
        val maxDelay = delayRange.value.endInclusive.toInt()
        zaloSendingEngine.startCampaign(
            messageTemplate = messageContent.value,
            delayMinSec = minDelay,
            delayMaxSec = maxDelay,
            autoAddFriend = autoAddFriend.value,
            sendOnlyIfUnsent = sendOnlyIfUnsent.value
        )
    }

    fun pauseCampaign() {
        zaloSendingEngine.pauseCampaign()
    }

    fun resumeCampaign() {
        zaloSendingEngine.resumeCampaign()
    }

    fun stopCampaign() {
        zaloSendingEngine.stopCampaign()
    }

}
