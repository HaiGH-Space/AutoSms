package com.gmail.nthai.domain.repository

import com.gmail.nthai.domain.model.BlacklistItem
import kotlinx.coroutines.flow.Flow

interface BlacklistRepository {
    fun getBlacklist(): Flow<List<BlacklistItem>>
    fun getBlacklistedPhonesFlow(): Flow<Set<String>>
    suspend fun getBlacklistedPhonesSet(): Set<String>
    suspend fun isPhoneBlacklisted(phone: String): Boolean
    suspend fun addToBlacklist(rawPhone: String, reason: String = ""): Long
    suspend fun addBatchToBlacklist(rawPhones: List<String>, defaultReason: String = "")
    suspend fun removeFromBlacklist(id: Long)
    suspend fun removeByPhone(phone: String)
    suspend fun clearBlacklist()
}
