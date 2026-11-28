package com.gmail.nthai.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.data.local.dao.ZaloRecipientStatsEntity
import com.gmail.nthai.data.local.mapper.toEntity
import com.gmail.nthai.domain.model.ImportMode
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.PendingImportState
import com.gmail.nthai.domain.model.SmsTemplate
import com.gmail.nthai.domain.model.ZaloRecipient
import com.gmail.nthai.domain.repository.BlacklistRepository
import com.gmail.nthai.domain.repository.TemplateRepository
import com.gmail.nthai.domain.repository.ZaloRepository
import com.gmail.nthai.domain.usecase.ImportCustomerExcelUseCase
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
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import javax.inject.Inject

@HiltViewModel
class ZaloViewModel @Inject constructor(
    private val zaloRepository: ZaloRepository,
    private val zaloSendingEngine: ZaloSendingEngine,
    private val templateRepository: TemplateRepository,
    private val blacklistRepository: BlacklistRepository,
    private val importCustomerExcelUseCase: ImportCustomerExcelUseCase
) : ViewModel() {

    val blacklistedPhones: StateFlow<Set<String>> = blacklistRepository.getBlacklistedPhonesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

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

    val templates: StateFlow<List<SmsTemplate>> = templateRepository.getAllTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedTemplateId = MutableStateFlow<Long?>(null)

    // Config state
    val messageContent = MutableStateFlow(
        "{Xin chào|Chào} {ten_khach}, bên em đang có chương trình ưu đãi tri ân khách hàng. Vui lòng nhắn tin lại để nhận mã giảm giá nhé!"
    )
    val delayRange = MutableStateFlow(20f..30f)
    val autoAddFriend = MutableStateFlow(false)
    val sendOnlyIfUnsent = MutableStateFlow(false)
    val isLowSpecDevice = MutableStateFlow(false)

    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

    private val _pendingImportState = MutableStateFlow<PendingImportState?>(null)
    val pendingImportState: StateFlow<PendingImportState?> = _pendingImportState.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importLoadingMessage = MutableStateFlow("")
    val importLoadingMessage: StateFlow<String> = _importLoadingMessage.asStateFlow()

    init {
        viewModelScope.launch {
            templates.collect { list ->
                if (list.isNotEmpty() && selectedTemplateId.value == null) {
                    val defaultSelected = list.firstOrNull { it.isSelected } ?: list.first()
                    selectedTemplateId.value = defaultSelected.id
                    if (messageContent.value.isBlank() || messageContent.value.startsWith("{Xin chào|Chào} {ten_khach}")) {
                        messageContent.value = defaultSelected.content
                    }
                }
            }
        }
    }

    fun selectTemplate(template: SmsTemplate) {
        selectedTemplateId.value = template.id
        messageContent.value = template.content
    }

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

    fun addToBlacklist(phone: String, reason: String = "Thêm từ danh sách Zalo") {
        viewModelScope.launch {
            blacklistRepository.addToBlacklist(phone, reason)
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

    fun prepareExcelImportWithUri(
        context: Context,
        uri: Uri,
        onHeaderError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            _isImporting.value = true
            _importLoadingMessage.value = "Đang mở file Excel..."
            try {
                val (bytes, headersResult) = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw java.io.IOException("Không thể mở file Excel đã chọn.")
                    val b = inputStream.use { it.readBytes() }
                    _importLoadingMessage.value = "Đang phân tích các cột trong file..."
                    val h = importCustomerExcelUseCase.getExcelHeaders(ByteArrayInputStream(b))
                    Pair(b, h)
                }
                _isImporting.value = false
                headersResult.onSuccess { headers ->
                    if (headers.isEmpty()) {
                        onHeaderError(IllegalArgumentException("File Excel không tìm thấy dòng tiêu đề."))
                    } else {
                        val matchedHeader = headers.find {
                            it.contains("sdt", ignoreCase = true) ||
                                    it.contains("sđt", ignoreCase = true) ||
                                    it.contains("phone", ignoreCase = true)
                        } ?: headers.first()

                        _pendingImportState.value = PendingImportState(
                            fileBytes = bytes,
                            headers = headers,
                            selectedHeader = matchedHeader
                        )
                    }
                }.onFailure { onHeaderError(it) }
            } catch (e: Exception) {
                _isImporting.value = false
                onHeaderError(e)
            }
        }
    }

    fun updateSelectedImportHeader(header: String) {
        val current = _pendingImportState.value ?: return
        _pendingImportState.value = current.copy(selectedHeader = header)
    }

    fun updateSelectedImportMode(mode: ImportMode) {
        val current = _pendingImportState.value ?: return
        _pendingImportState.value = current.copy(selectedImportMode = mode)
    }

    fun cancelImport() {
        _pendingImportState.value = null
    }

    fun confirmImport(onResult: (Result<Int>) -> Unit) {
        val current = _pendingImportState.value ?: return
        _pendingImportState.value = null
        viewModelScope.launch {
            _isImporting.value = true
            _importLoadingMessage.value = "Đang phân tích và nhập danh sách Zalo..."
            try {
                val result = withContext(Dispatchers.IO) {
                    val inputStream = ByteArrayInputStream(current.fileBytes)
                    val parseResult = importCustomerExcelUseCase(inputStream, current.selectedHeader)
                    parseResult.map { parsedCustomers ->
                        val recipients = parsedCustomers.map { customer ->
                            val name = customer.customFields.entries.find { (k, _) ->
                                k.contains("tên", ignoreCase = true) || k.contains("ten", ignoreCase = true)
                            }?.value ?: "Khách hàng"

                            ZaloRecipient(
                                name = name,
                                phone = customer.phone,
                                customFieldsJson = customer.toEntity().customFieldsJson
                            )
                        }
                        _importLoadingMessage.value = "Đang lưu ${recipients.size} SĐT Zalo vào cơ sở dữ liệu..."
                        zaloRepository.importRecipients(
                            recipients,
                            current.selectedImportMode
                        )
                    }
                }
                _isImporting.value = false
                result.onSuccess { count ->
                    onResult(Result.success(count))
                }.onFailure { error ->
                    onResult(Result.failure(error))
                }
            } catch (error: Exception) {
                _isImporting.value = false
                onResult(Result.failure(error))
            }
        }
    }

    fun startCampaign(): Boolean {
        if (!_isAccessibilityEnabled.value) {
            return false
        }
        val minDelay = delayRange.value.start.toInt()
        val maxDelay = delayRange.value.endInclusive.toInt()
        zaloSendingEngine.startCampaign(
            messageTemplate = messageContent.value,
            delayMinSec = minDelay,
            delayMaxSec = maxDelay,
            autoAddFriend = autoAddFriend.value,
            sendOnlyIfUnsent = sendOnlyIfUnsent.value,
            isLowSpecDevice = isLowSpecDevice.value
        )
        return true
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

    fun clearLogs() {
        viewModelScope.launch {
            zaloRepository.clearLogs()
        }
    }
}
