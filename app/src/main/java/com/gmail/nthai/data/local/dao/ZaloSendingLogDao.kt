package com.gmail.nthai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gmail.nthai.data.local.entity.ZaloSendingLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ZaloSendingLogDao {
    @Query("SELECT * FROM zalo_sending_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ZaloSendingLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ZaloSendingLogEntity): Long

    @Query("DELETE FROM zalo_sending_logs")
    suspend fun deleteAllLogs()
}
