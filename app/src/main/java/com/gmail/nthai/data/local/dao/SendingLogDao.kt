package com.gmail.nthai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gmail.nthai.data.local.entity.SendingLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SendingLogDao {
    @Query("SELECT * FROM sending_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SendingLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SendingLogEntity): Long

    @Update
    suspend fun updateLog(log: SendingLogEntity)

    @Query("UPDATE sending_logs SET status = :status, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateLogStatus(id: Long, status: String, errorMessage: String?)

    @Query("SELECT * FROM sending_logs WHERE customerId = :customerId")
    suspend fun getLogsForCustomer(customerId: Long): List<SendingLogEntity>

    @Query("DELETE FROM sending_logs")
    suspend fun deleteAllLogs()
}
