package com.gmail.nthai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.repository.SendingLogRepository
import com.gmail.nthai.domain.usecase.ExportReportExcelUseCase
import com.gmail.nthai.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import javax.inject.Inject

data class ReportStats(
    val total: Int = 0,
    val success: Int = 0,
    val failed: Int = 0,
    val pending: Int = 0,
    val successRatePercentage: Float = 0f
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val sendingLogRepository: SendingLogRepository,
    private val exportReportExcelUseCase: ExportReportExcelUseCase
) : ViewModel() {

    val logs: StateFlow<List<LogRecord>> = sendingLogRepository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSimSlot = MutableStateFlow<Int?>(null)
    val selectedSimSlot: StateFlow<Int?> = _selectedSimSlot.asStateFlow()

    private val _rangeStartMillis = MutableStateFlow<Long?>(null)
    val rangeStartMillis: StateFlow<Long?> = _rangeStartMillis.asStateFlow()

    private val _rangeEndMillis = MutableStateFlow<Long?>(null)
    val rangeEndMillis: StateFlow<Long?> = _rangeEndMillis.asStateFlow()

    val filteredLogs: StateFlow<List<LogRecord>> = combine(
        logs,
        _selectedSimSlot,
        _rangeStartMillis,
        _rangeEndMillis
    ) { allLogs, simSlot, startMillis, endMillis ->
        val start = startMillis?.let(DateTimeUtils::localStartOfDay)
        val endExclusive = endMillis?.let(DateTimeUtils::localStartOfNextDay)
        allLogs.filter { log ->
            (simSlot == null || log.simSlot == simSlot) &&
                (start == null || log.timestamp >= start) &&
                (endExclusive == null || log.timestamp < endExclusive)
        }
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<ReportStats> = filteredLogs.map { list ->
        val total = list.size
        val success = list.count { it.status == LogStatus.SENT_SUCCESS }
        val failed = list.count { it.status == LogStatus.FAILED }
        val pending = list.count { it.status == LogStatus.PENDING }
        val rate = if (total > 0) (success.toFloat() / total) * 100f else 0f

        ReportStats(
            total = total,
            success = success,
            failed = failed,
            pending = pending,
            successRatePercentage = rate
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportStats())

    fun setSelectedSimSlot(slot: Int?) {
        _selectedSimSlot.value = slot
    }

    fun setDateRange(start: Long?, end: Long?) {
        _rangeStartMillis.value = start
        _rangeEndMillis.value = end
    }

    fun clearFilters() {
        _selectedSimSlot.value = null
        _rangeStartMillis.value = null
        _rangeEndMillis.value = null
    }

    suspend fun exportExcel(logs: List<LogRecord>, outputStream: OutputStream): Result<Unit> =
        withContext(Dispatchers.IO) {
            exportReportExcelUseCase(logs, outputStream)
        }

    fun clearLogs() {
        viewModelScope.launch {
            sendingLogRepository.deleteAllLogs()
        }
    }
}
