package com.gmail.nthai.presentation.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.engine.AutoCallEngine
import com.gmail.nthai.domain.model.AutoCallConfig
import com.gmail.nthai.domain.model.AutoCallProgress
import com.gmail.nthai.domain.model.CallLogRecord
import com.gmail.nthai.domain.model.CallRecipient
import com.gmail.nthai.domain.model.ImportMode
import com.gmail.nthai.domain.model.PendingImportState
import com.gmail.nthai.domain.model.SimConfig
import com.gmail.nthai.domain.repository.AutoCallRepository
import com.gmail.nthai.domain.repository.BlacklistRepository
import com.gmail.nthai.domain.repository.CallRecipientRepository
import com.gmail.nthai.domain.repository.CallRecipientStats
import com.gmail.nthai.domain.repository.SimConfigRepository
import com.gmail.nthai.domain.usecase.ImportCustomerExcelUseCase
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
class AutoCallViewModel @Inject constructor(
    private val autoCallEngine: AutoCallEngine,
    private val callRecipientRepository: CallRecipientRepository,
    private val autoCallRepository: AutoCallRepository,
    simConfigRepository: SimConfigRepository,
    private val blacklistRepository: BlacklistRepository,
    private val importCustomerExcelUseCase: ImportCustomerExcelUseCase,
) : ViewModel() {

    val progressState: StateFlow<AutoCallProgress> = autoCallEngine.progressState

    val callLogs: StateFlow<List<CallLogRecord>> = autoCallRepository.getAllCallLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipients: StateFlow<List<CallRecipient>> = callRecipientRepository.getAllRecipients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<CallRecipientStats> = callRecipientRepository.observeStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CallRecipientStats(0, 0, 0, 0, 0))

    val blacklistedPhones: StateFlow<Set<String>> = blacklistRepository.getBlacklistedPhonesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val simConfigs: StateFlow<List<SimConfig>> = simConfigRepository.getSimConfigs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _delaySeconds = MutableStateFlow(5)
    val delaySeconds: StateFlow<Int> = _delaySeconds.asStateFlow()

    private val _selectedSimSlot = MutableStateFlow(1)
    val selectedSimSlot: StateFlow<Int> = _selectedSimSlot.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _pendingImportState = MutableStateFlow<PendingImportState?>(null)
    val pendingImportState: StateFlow<PendingImportState?> = _pendingImportState.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importLoadingMessage = MutableStateFlow("")
    val importLoadingMessage: StateFlow<String> = _importLoadingMessage.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun updateDelay(seconds: Int) {
        if ((seconds in 1..120)) {
            _delaySeconds.value = seconds
        }
    }

    fun updateSimSlot(slot: Int) {
        if (slot in 1..2) {
            _selectedSimSlot.value = slot
        }
    }

    fun toggleRecipientSelection(id: Long, currentSelection: Boolean) {
        viewModelScope.launch {
            callRecipientRepository.updateRecipientSelection(id, !currentSelection)
        }
    }

    fun selectAllRecipients(selectAll: Boolean) {
        viewModelScope.launch {
            callRecipientRepository.updateAllRecipientSelection(selectAll)
        }
    }

    fun addRecipient(name: String, phone: String) {
        viewModelScope.launch {
            callRecipientRepository.insertRecipient(name, phone)
        }
    }

    fun deleteRecipient(id: Long) {
        viewModelScope.launch {
            callRecipientRepository.deleteRecipient(id)
        }
    }

    fun deleteAllRecipients() {
        viewModelScope.launch {
            callRecipientRepository.deleteAllRecipients()
        }
    }

    fun resetStatus() {
        viewModelScope.launch {
            callRecipientRepository.resetAllRecipientStatus()
        }
    }

    fun addToBlacklist(phone: String, reason: String = "Thêm từ màn hình gọi điện") {
        viewModelScope.launch {
            blacklistRepository.addToBlacklist(phone, reason)
        }
    }

    // Excel import
    fun prepareExcelImportWithUri(context: Context, uri: Uri, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            _isImporting.value = true
            _importLoadingMessage.value = "Đang đọc tiêu đề file Excel..."
            try {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } ?: throw IllegalArgumentException("Không thể đọc tệp Excel đã chọn")

                val headers = withContext(Dispatchers.IO) {
                    val stream = ByteArrayInputStream(bytes)
                    importCustomerExcelUseCase.getExcelHeaders(stream).getOrThrow()
                }

                val detectedPhoneCol = headers.firstOrNull { h ->
                    val lower = h.lowercase()
                    lower.contains("sdt") || lower.contains("sđt") || lower.contains("phone") || lower.contains("thoại")
                } ?: headers.firstOrNull() ?: ""

                val detectedNameCol = headers.firstOrNull { h ->
                    val lower = h.lowercase()
                    lower != detectedPhoneCol.lowercase() && (
                        lower.contains("tên") || lower.contains("ten") || lower.contains("name") || lower.contains("họ") || lower.contains("ho")
                    )
                } ?: ""

                _pendingImportState.value = PendingImportState(
                    fileBytes = bytes,
                    headers = headers,
                    selectedHeader = detectedPhoneCol,
                    selectedNameHeader = detectedNameCol,
                    selectedImportMode = ImportMode.REPLACE_ALL
                )
                _isImporting.value = false
            } catch (e: Exception) {
                _isImporting.value = false
                onError(e)
            }
        }
    }

    fun updateSelectedImportHeader(header: String) {
        val current = _pendingImportState.value ?: return
        _pendingImportState.value = current.copy(selectedHeader = header)
    }

    fun updateSelectedImportNameHeader(header: String) {
        val current = _pendingImportState.value ?: return
        _pendingImportState.value = current.copy(selectedNameHeader = header)
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
            _importLoadingMessage.value = "Đang phân tích và nhập danh sách gọi điện..."
            try {
                val result = withContext(Dispatchers.IO) {
                    val stream = ByteArrayInputStream(current.fileBytes)
                    callRecipientRepository.importFromExcel(
                        stream,
                        current.selectedHeader,
                        current.selectedNameHeader.ifBlank { null },
                        current.selectedImportMode
                    )
                }
                _isImporting.value = false
                onResult(result)
            } catch (e: Exception) {
                _isImporting.value = false
                onResult(Result.failure(e))
            }
        }
    }

    fun startCampaign() {
        viewModelScope.launch {
            val allList = recipients.value
            val selected = allList.filter { it.isSelected }
            val listToCall = selected.ifEmpty { allList }
            val validList = listToCall.filter { it.phone.replace(Regex("[^0-9+]"), "").isNotBlank() }

            val config = AutoCallConfig(
                delaySeconds = _delaySeconds.value,
                selectedCustomerIds = validList.map { it.id },
                simSlot = _selectedSimSlot.value
            )
            autoCallEngine.startCampaign(config, validList)
        }
    }

    fun pauseCampaign() {
        autoCallEngine.pauseCampaign()
    }

    fun resumeCampaign() {
        autoCallEngine.resumeCampaign()
    }

    fun stopCampaign() {
        autoCallEngine.stopCampaign()
    }

    fun clearCallLogs() {
        viewModelScope.launch {
            autoCallRepository.deleteAllCallLogs()
        }
    }
}
