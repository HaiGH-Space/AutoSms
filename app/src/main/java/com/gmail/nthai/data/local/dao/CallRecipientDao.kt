package com.gmail.nthai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gmail.nthai.data.local.entity.CallRecipientEntity
import kotlinx.coroutines.flow.Flow

data class CallRecipientStatsEntity(
    val totalCount: Int,
    val pendingCount: Int,
    val calledCount: Int,
    val failedCount: Int,
    val selectedCount: Int
)

@Dao
interface CallRecipientDao {
    @Query(
        """
        SELECT 
            COUNT(*) AS totalCount,
            COALESCE(SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END), 0) AS pendingCount,
            COALESCE(SUM(CASE WHEN status = 'SENT_SUCCESS' OR status = 'CALLED' THEN 1 ELSE 0 END), 0) AS calledCount,
            COALESCE(SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END), 0) AS failedCount,
            COALESCE(SUM(CASE WHEN isSelected = 1 THEN 1 ELSE 0 END), 0) AS selectedCount
        FROM call_recipients
        """
    )
    fun observeRecipientStats(): Flow<CallRecipientStatsEntity>

    @Query("SELECT * FROM call_recipients ORDER BY id ASC")
    fun getAllRecipients(): Flow<List<CallRecipientEntity>>

    @Query("SELECT phone FROM call_recipients")
    suspend fun getAllRecipientPhones(): List<String>

    @Query("SELECT * FROM call_recipients WHERE isSelected = 1 ORDER BY id ASC")
    suspend fun getSelectedRecipientsList(): List<CallRecipientEntity>

    @Query("SELECT * FROM call_recipients WHERE id = :id")
    suspend fun getRecipientById(id: Long): CallRecipientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipient(recipient: CallRecipientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipients(recipients: List<CallRecipientEntity>)

    @Update
    suspend fun updateRecipient(recipient: CallRecipientEntity)

    @Query("UPDATE call_recipients SET isSelected = :isSelected WHERE id = :id")
    suspend fun updateRecipientSelection(id: Long, isSelected: Boolean)

    @Query("UPDATE call_recipients SET isSelected = :isSelected")
    suspend fun updateAllRecipientSelection(isSelected: Boolean)

    @Query("UPDATE call_recipients SET status = :status, callDurationSeconds = :duration, note = :note, lastCallTime = :lastCallTime WHERE id = :id")
    suspend fun updateRecipientStatus(
        id: Long,
        status: String,
        duration: Int,
        note: String,
        lastCallTime: Long
    )

    @Query("UPDATE call_recipients SET status = 'PENDING'")
    suspend fun resetAllRecipientStatus()

    @Delete
    suspend fun deleteRecipient(recipient: CallRecipientEntity)

    @Query("DELETE FROM call_recipients WHERE id = :id")
    suspend fun deleteRecipientById(id: Long)

    @Query("DELETE FROM call_recipients")
    suspend fun deleteAllRecipients()
}
