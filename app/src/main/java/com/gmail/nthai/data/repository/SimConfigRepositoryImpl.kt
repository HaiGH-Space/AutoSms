package com.gmail.nthai.data.repository

import com.gmail.nthai.data.local.dao.SimConfigDao
import com.gmail.nthai.data.local.mapper.toDomain
import com.gmail.nthai.data.local.mapper.toEntity
import com.gmail.nthai.domain.model.SendingRule
import com.gmail.nthai.domain.model.SimConfig
import com.gmail.nthai.domain.repository.SimConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SimConfigRepositoryImpl(
    private val simConfigDao: SimConfigDao
) : SimConfigRepository {

    override fun getSimConfigs(): Flow<List<SimConfig>> {
        return simConfigDao.getSimConfigs().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getSimConfigBySlot(simSlot: Int): SimConfig? {
        return simConfigDao.getSimConfigBySlot(simSlot)?.toDomain()
    }

    override suspend fun saveSimConfig(config: SimConfig) {
        simConfigDao.saveSimConfig(config.toEntity())
    }

    override suspend fun incrementSentCount(simSlot: Int) {
        simConfigDao.incrementSentCount(simSlot)
    }

    override suspend fun resetDailyCountsIfNewDay(todayDate: String) {
        simConfigDao.resetDailyCountsIfNewDay(todayDate)
    }

    override fun getSendingRule(): Flow<SendingRule> {
        return simConfigDao.getSendingRule().map { it?.toDomain() ?: SendingRule() }
    }

    override suspend fun saveSendingRule(rule: SendingRule) {
        simConfigDao.saveSendingRule(rule.toEntity())
    }
}
