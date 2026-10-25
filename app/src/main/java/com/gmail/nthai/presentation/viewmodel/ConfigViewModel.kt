package com.gmail.nthai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.data.sms.SimManagerWrapper
import com.gmail.nthai.domain.model.SendingRule
import com.gmail.nthai.domain.model.SimConfig
import com.gmail.nthai.domain.repository.SimConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val simConfigRepository: SimConfigRepository,
    private val simManagerWrapper: SimManagerWrapper
) : ViewModel() {

    val sendingRule: StateFlow<SendingRule> = simConfigRepository.getSendingRule()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SendingRule())

    private val _simConfigs = MutableStateFlow<List<SimConfig>>(emptyList())
    val simConfigs: StateFlow<List<SimConfig>> = _simConfigs
    private var loadSimsJob: Job? = null

    init {
        loadSims()
    }

    fun loadSims() {
        loadSimsJob?.cancel()
        loadSimsJob = viewModelScope.launch {
            val detectedSims = try {
                simManagerWrapper.getActiveSims()
            } catch (_: SecurityException) {
                emptyList()
            }

            val savedSims = try {
                simConfigRepository.getSimConfigs().first()
            } catch (_: Exception) {
                emptyList()
            }

            val merged = mutableListOf<SimConfig>()
            for (slot in 1..2) {
                val detected = detectedSims.find { it.simSlot == slot }
                val saved = savedSims.find { it.simSlot == slot }

                val targetSubId = detected?.subscriptionId ?: saved?.subscriptionId ?: -1
                val targetLimit = saved?.dailyLimit ?: detected?.dailyLimit ?: 100
                val sentToday = saved?.sentCountToday ?: 0
                val lastReset = saved?.lastResetDate ?: ""

                val config = SimConfig(
                    subscriptionId = targetSubId,
                    simSlot = slot,
                    dailyLimit = targetLimit,
                    sentCountToday = sentToday,
                    lastResetDate = lastReset,
                    carrierName = detected?.carrierName.orEmpty()
                )

                merged.add(config)
                simConfigRepository.saveSimConfig(config)
            }

            _simConfigs.value = merged
        }
    }

    fun updateSimLimit(simSlot: Int, limit: Int) {
        viewModelScope.launch {
            val current = _simConfigs.value.find { it.simSlot == simSlot }
                ?: SimConfig(subscriptionId = -1, simSlot = simSlot)
            val updated = current.copy(dailyLimit = limit)
            simConfigRepository.saveSimConfig(updated)
            loadSims()
        }
    }

    fun updateRule(minDelay: Int, maxDelay: Int, startHour: Int, endHour: Int, phoneColumnName: String = "sdt") {
        viewModelScope.launch {
            val newRule = SendingRule(
                minDelaySec = minDelay,
                maxDelaySec = maxDelay,
                startHour = startHour,
                endHour = endHour,
                phoneColumnName = phoneColumnName.ifBlank { "sdt" }
            )
            simConfigRepository.saveSendingRule(newRule)
        }
    }
}
