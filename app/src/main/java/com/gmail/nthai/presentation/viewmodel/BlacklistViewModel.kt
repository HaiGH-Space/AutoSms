package com.gmail.nthai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.model.BlacklistItem
import com.gmail.nthai.domain.repository.BlacklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlacklistViewModel @Inject constructor(
    private val blacklistRepository: BlacklistRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val blacklistItems: StateFlow<List<BlacklistItem>> = combine(
        blacklistRepository.getBlacklist(),
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.trim()
            list.filter { item ->
                item.phone.contains(q, ignoreCase = true) ||
                        item.rawPhone.contains(q, ignoreCase = true) ||
                        item.reason.contains(q, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addPhone(rawPhone: String, reason: String = "") {
        if (rawPhone.isBlank()) return
        viewModelScope.launch {
            blacklistRepository.addToBlacklist(rawPhone, reason)
        }
    }

    fun addBatch(rawPhonesText: String, defaultReason: String = "") {
        val phones = rawPhonesText.split("\n", ",", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (phones.isEmpty()) return
        viewModelScope.launch {
            blacklistRepository.addBatchToBlacklist(phones, defaultReason)
        }
    }

    fun deletePhone(id: Long) {
        viewModelScope.launch {
            blacklistRepository.removeFromBlacklist(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            blacklistRepository.clearBlacklist()
        }
    }
}
