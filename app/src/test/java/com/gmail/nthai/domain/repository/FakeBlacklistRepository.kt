package com.gmail.nthai.domain.repository

import com.gmail.nthai.domain.model.BlacklistItem
import com.gmail.nthai.util.PhoneNumberUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeBlacklistRepository : BlacklistRepository {
    private val blacklistedSet = MutableStateFlow<Set<String>>(emptySet())
    private val itemsFlow = MutableStateFlow<List<BlacklistItem>>(emptyList())

    override fun getBlacklist(): Flow<List<BlacklistItem>> = itemsFlow.asStateFlow()

    override fun getBlacklistedPhonesFlow(): Flow<Set<String>> = blacklistedSet.asStateFlow()

    override suspend fun getBlacklistedPhonesSet(): Set<String> = blacklistedSet.value

    override suspend fun isPhoneBlacklisted(phone: String): Boolean {
        val normalized = PhoneNumberUtils.normalizePhoneNumber(phone)
        return blacklistedSet.value.contains(normalized)
    }

    override suspend fun addToBlacklist(rawPhone: String, reason: String): Long {
        val normalized = PhoneNumberUtils.normalizePhoneNumber(rawPhone)
        if (normalized.isNotBlank()) {
            blacklistedSet.value = blacklistedSet.value + normalized
            val newItem = BlacklistItem(
                id = (itemsFlow.value.size + 1).toLong(),
                phone = normalized,
                rawPhone = rawPhone,
                reason = reason
            )
            itemsFlow.value = itemsFlow.value + newItem
        }
        return 1L
    }

    override suspend fun addBatchToBlacklist(rawPhones: List<String>, defaultReason: String) {
        rawPhones.forEach { addToBlacklist(it, defaultReason) }
    }

    override suspend fun removeFromBlacklist(id: Long) {
        val item = itemsFlow.value.find { it.id == id }
        if (item != null) {
            blacklistedSet.value = blacklistedSet.value - item.phone
            itemsFlow.value = itemsFlow.value.filter { it.id != id }
        }
    }

    override suspend fun removeByPhone(phone: String) {
        val normalized = PhoneNumberUtils.normalizePhoneNumber(phone)
        blacklistedSet.value = blacklistedSet.value - normalized
        itemsFlow.value = itemsFlow.value.filter { it.phone != normalized }
    }

    override suspend fun clearBlacklist() {
        blacklistedSet.value = emptySet()
        itemsFlow.value = emptyList()
    }
}
