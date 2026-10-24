package com.gmail.nthai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gmail.nthai.data.local.entity.SendingRuleEntity
import com.gmail.nthai.data.local.entity.SimConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SimConfigDao {
    @Query("SELECT * FROM sim_configs ORDER BY simSlot ASC")
    fun getSimConfigs(): Flow<List<SimConfigEntity>>

    @Query("SELECT * FROM sim_configs WHERE simSlot = :simSlot LIMIT 1")
    suspend fun getSimConfigBySlot(simSlot: Int): SimConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSimConfig(config: SimConfigEntity)

    @Query("UPDATE sim_configs SET sentCountToday = sentCountToday + 1 WHERE simSlot = :simSlot")
    suspend fun incrementSentCount(simSlot: Int)

    @Query("UPDATE sim_configs SET sentCountToday = 0, lastResetDate = :todayDate WHERE lastResetDate != :todayDate")
    suspend fun resetDailyCountsIfNewDay(todayDate: String)

    @Query("SELECT * FROM sending_rules WHERE id = 1 LIMIT 1")
    fun getSendingRule(): Flow<SendingRuleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSendingRule(rule: SendingRuleEntity)
}
