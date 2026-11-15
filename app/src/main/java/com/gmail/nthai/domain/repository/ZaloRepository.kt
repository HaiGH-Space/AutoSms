package com.gmail.nthai.domain.repository

import com.gmail.nthai.data.local.dao.ZaloRecipientStatsEntity
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.ZaloRecipient
import kotlinx.coroutines.flow.Flow

interface ZaloRepository {
    fun observeStats(): Flow<ZaloRecipientStatsEntity>
    fun observeRecipients(): Flow<List<ZaloRecipient>>
    fun observeLogs(): Flow<List<LogRecord>>
    suspend fun getPendingRecipients(): List<ZaloRecipient>
    suspend fun insertRecipients(recipients: List<ZaloRecipient>)
    suspend fun insertRecipient(recipient: ZaloRecipient): Long
    suspend fun updateRecipientStatus(id: Long, status: String, note: String)
    suspend fun deleteRecipient(id: Long)
    suspend fun deleteAllRecipients()
    suspend fun resetAllRecipientStatus()
    suspend fun resetRecipientStatus(id: Long)
    suspend fun addLog(log: LogRecord): Long
    suspend fun clearLogs()
}
