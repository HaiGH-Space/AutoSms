package com.gmail.nthai.domain.repository

import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import kotlinx.coroutines.flow.Flow

interface SendingLogRepository {
    fun getAllLogs(): Flow<List<LogRecord>>
    suspend fun insertLog(log: LogRecord): Long
    suspend fun updateLogStatus(id: Long, status: LogStatus, errorMessage: String? = null)
    suspend fun getLogsForCustomer(customerId: Long): List<LogRecord>
    suspend fun deleteAllLogs()
}
