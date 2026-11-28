package com.gmail.nthai.data.repository

import com.gmail.nthai.data.local.dao.BlacklistDao
import com.gmail.nthai.data.local.entity.BlacklistEntity
import com.gmail.nthai.data.local.mapper.toDomain
import com.gmail.nthai.domain.model.BlacklistItem
import com.gmail.nthai.domain.repository.BlacklistRepository
import com.gmail.nthai.util.PhoneNumberUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlacklistRepositoryImpl @Inject constructor(
    private val blacklistDao: BlacklistDao
) : BlacklistRepository {

    override fun getBlacklist(): Flow<List<BlacklistItem>> {
        return blacklistDao.getAllBlacklist().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBlacklistedPhonesFlow(): Flow<Set<String>> {
        return blacklistDao.getAllPhonesFlow().map { it.toSet() }
    }

    override suspend fun getBlacklistedPhonesSet(): Set<String> {
        return blacklistDao.getAllNormalizedPhones().toSet()
    }

    override suspend fun isPhoneBlacklisted(phone: String): Boolean {
        val normalized = PhoneNumberUtils.normalizePhoneNumber(phone)
        if (normalized.isBlank()) return false
        return blacklistDao.isBlacklisted(normalized)
    }

    override suspend fun addToBlacklist(rawPhone: String, reason: String): Long {
        val normalized = PhoneNumberUtils.normalizePhoneNumber(rawPhone)
        if (normalized.isBlank()) return 0L
        val entity = BlacklistEntity(
            phone = normalized,
            rawPhone = rawPhone.trim(),
            reason = reason.trim(),
            createdAt = System.currentTimeMillis()
        )
        return blacklistDao.insert(entity)
    }

    override suspend fun addBatchToBlacklist(rawPhones: List<String>, defaultReason: String) {
        val entities = rawPhones.mapNotNull { raw ->
            val normalized = PhoneNumberUtils.normalizePhoneNumber(raw)
            if (normalized.isNotBlank()) {
                BlacklistEntity(
                    phone = normalized,
                    rawPhone = raw.trim(),
                    reason = defaultReason.trim(),
                    createdAt = System.currentTimeMillis()
                )
            } else null
        }
        if (entities.isNotEmpty()) {
            blacklistDao.insertAll(entities)
        }
    }

    override suspend fun removeFromBlacklist(id: Long) {
        blacklistDao.deleteById(id)
    }

    override suspend fun removeByPhone(phone: String) {
        val normalized = PhoneNumberUtils.normalizePhoneNumber(phone)
        if (normalized.isNotBlank()) {
            blacklistDao.deleteByPhone(normalized)
        }
    }

    override suspend fun clearBlacklist() {
        blacklistDao.deleteAll()
    }
}
