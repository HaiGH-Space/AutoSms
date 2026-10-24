package com.gmail.nthai.domain.repository

import com.gmail.nthai.domain.model.SendingRule
import com.gmail.nthai.domain.model.SimConfig
import kotlinx.coroutines.flow.Flow

interface SimConfigRepository {
    fun getSimConfigs(): Flow<List<SimConfig>>
    suspend fun getSimConfigBySlot(simSlot: Int): SimConfig?
    suspend fun saveSimConfig(config: SimConfig)
    suspend fun incrementSentCount(simSlot: Int)
    suspend fun resetDailyCountsIfNewDay(todayDate: String)
    fun getSendingRule(): Flow<SendingRule>
    suspend fun saveSendingRule(rule: SendingRule)
}
