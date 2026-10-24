package com.gmail.nthai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.repository.SendingLogRepository
import com.gmail.nthai.domain.usecase.ExportReportExcelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    val stats: StateFlow<ReportStats> = logs.map { list ->
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportStats())

    fun exportExcel(outputStream: OutputStream, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = exportReportExcelUseCase(logs.value, outputStream)
            onResult(result)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            sendingLogRepository.deleteAllLogs()
        }
    }
}
