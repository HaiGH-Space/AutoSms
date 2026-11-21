package com.gmail.nthai.domain.repository

import com.gmail.nthai.domain.model.CallRecipient
import com.gmail.nthai.domain.model.ImportMode
import com.gmail.nthai.domain.model.LogStatus
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

data class CallRecipientStats(
    val totalCount: Int,
    val pendingCount: Int,
    val calledCount: Int,
    val failedCount: Int,
    val selectedCount: Int
)

interface CallRecipientRepository {
    fun getAllRecipients(): Flow<List<CallRecipient>>
    fun observeStats(): Flow<CallRecipientStats>
    suspend fun getSelectedRecipients(): List<CallRecipient>
    suspend fun insertRecipient(name: String, phone: String): Long
    suspend fun updateRecipientSelection(id: Long, isSelected: Boolean)
    suspend fun updateAllRecipientSelection(isSelected: Boolean)
    suspend fun updateRecipientStatus(id: Long, status: LogStatus, durationSeconds: Int = 0, note: String = "")
    suspend fun resetAllRecipientStatus()
    suspend fun deleteRecipient(id: Long)
    suspend fun deleteAllRecipients()
    suspend fun importFromExcel(
        inputStream: InputStream,
        phoneColumn: String,
        importMode: ImportMode
    ): Result<Int>
}
