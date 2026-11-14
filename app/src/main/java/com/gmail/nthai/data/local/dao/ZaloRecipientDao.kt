package com.gmail.nthai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gmail.nthai.data.local.entity.ZaloRecipientEntity
import kotlinx.coroutines.flow.Flow

data class ZaloRecipientStatsEntity(
    val totalCount: Int,
    val pendingCount: Int,
    val successCount: Int,
    val failedCount: Int
)

@Dao
interface ZaloRecipientDao {
    @Query(
        """
        SELECT 
            COUNT(*) AS totalCount,
            COALESCE(SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END), 0) AS pendingCount,
            COALESCE(SUM(CASE WHEN status = 'SENT_SUCCESS' THEN 1 ELSE 0 END), 0) AS successCount,
            COALESCE(SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END), 0) AS failedCount
        FROM zalo_recipients
        """
    )
    fun observeRecipientStats(): Flow<ZaloRecipientStatsEntity>

    @Query("SELECT * FROM zalo_recipients ORDER BY id ASC")
    fun getAllRecipients(): Flow<List<ZaloRecipientEntity>>

    @Query("SELECT * FROM zalo_recipients WHERE status = 'PENDING' ORDER BY id ASC")
    fun getPendingRecipients(): Flow<List<ZaloRecipientEntity>>

    @Query("SELECT * FROM zalo_recipients WHERE status = 'PENDING' ORDER BY id ASC")
    suspend fun getPendingRecipientsList(): List<ZaloRecipientEntity>

    @Query("SELECT * FROM zalo_recipients WHERE id = :id")
    suspend fun getRecipientById(id: Long): ZaloRecipientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipient(recipient: ZaloRecipientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipients(recipients: List<ZaloRecipientEntity>)

    @Update
    suspend fun updateRecipient(recipient: ZaloRecipientEntity)

    @Query("UPDATE zalo_recipients SET status = :status, note = :note, lastSentTime = :lastSentTime WHERE id = :id")
    suspend fun updateRecipientStatus(id: Long, status: String, note: String, lastSentTime: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteRecipient(recipient: ZaloRecipientEntity)

    @Query("DELETE FROM zalo_recipients WHERE id = :id")
    suspend fun deleteRecipientById(id: Long)

    @Query("DELETE FROM zalo_recipients")
    suspend fun deleteAllRecipients()

    @Query("UPDATE zalo_recipients SET status = 'PENDING', note = 'Chờ gửi'")
    suspend fun resetAllRecipientStatus()

    @Query("UPDATE zalo_recipients SET status = 'PENDING', note = 'Chờ gửi lại' WHERE id = :id")
    suspend fun resetRecipientStatusById(id: Long)
}
