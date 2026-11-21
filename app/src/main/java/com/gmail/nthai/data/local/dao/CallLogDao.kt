package com.gmail.nthai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gmail.nthai.data.local.entity.CallLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(log: CallLogEntity): Long

    @Query("SELECT * FROM call_logs WHERE id = :id LIMIT 1")
    suspend fun getCallLogById(id: Long): CallLogEntity?

    @Query("UPDATE call_logs SET status = :status, callDurationSeconds = :durationSeconds, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateCallLogStatus(id: Long, status: String, durationSeconds: Int, errorMessage: String?)

    @Query("DELETE FROM call_logs")
    suspend fun deleteAllCallLogs()
}
