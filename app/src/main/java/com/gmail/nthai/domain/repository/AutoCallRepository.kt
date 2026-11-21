package com.gmail.nthai.domain.repository

import com.gmail.nthai.domain.model.CallLogRecord
import kotlinx.coroutines.flow.Flow

interface AutoCallRepository {
    fun getAllCallLogs(): Flow<List<CallLogRecord>>
    suspend fun insertCallLog(record: CallLogRecord): Long
    suspend fun updateCallLogStatus(id: Long, status: String, durationSeconds: Int, errorMessage: String?)
    suspend fun deleteAllCallLogs()
}
