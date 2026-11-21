package com.gmail.nthai.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.engine.AutoCallEngine
import com.gmail.nthai.domain.model.AutoCallConfig
import com.gmail.nthai.domain.model.AutoCallProgress
import com.gmail.nthai.domain.model.CallLogRecord
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.repository.AutoCallRepository
import com.gmail.nthai.domain.repository.CustomerRepository
import com.gmail.nthai.domain.model.SimConfig
import com.gmail.nthai.domain.repository.SimConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutoCallViewModel @Inject constructor(
    private val autoCallEngine: AutoCallEngine,
    private val customerRepository: CustomerRepository,
    private val autoCallRepository: AutoCallRepository,
    private val simConfigRepository: SimConfigRepository
) : ViewModel() {

    val progressState: StateFlow<AutoCallProgress> = autoCallEngine.progressState

    val callLogs: StateFlow<List<CallLogRecord>> = autoCallRepository.getAllCallLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = customerRepository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val simConfigs: StateFlow<List<SimConfig>> = simConfigRepository.getSimConfigs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _delaySeconds = MutableStateFlow(5)
    val delaySeconds: StateFlow<Int> = _delaySeconds.asStateFlow()

    private val _selectedSimSlot = MutableStateFlow(1)
    val selectedSimSlot: StateFlow<Int> = _selectedSimSlot.asStateFlow()

    fun updateDelay(seconds: Int) {
        if (seconds in 1..120) {
            _delaySeconds.value = seconds
        }
    }

    fun updateSimSlot(slot: Int) {
        if (slot in 1..2) {
            _selectedSimSlot.value = slot
        }
    }

    fun startCampaign() {
        viewModelScope.launch {
            val selected = customers.value.filter { it.isSelected }
            val listToCall = (if (selected.isNotEmpty()) selected else customers.value)
                .filter { it.phone.replace(Regex("[^0-9+]"), "").isNotBlank() }
            val config = AutoCallConfig(
                delaySeconds = _delaySeconds.value,
                selectedCustomerIds = listToCall.map { it.id },
                simSlot = _selectedSimSlot.value
            )
            autoCallEngine.startCampaign(config, listToCall)
        }
    }

    fun pauseCampaign() {
        autoCallEngine.pauseCampaign()
    }

    fun resumeCampaign() {
        autoCallEngine.resumeCampaign()
    }

    fun stopCampaign() {
        autoCallEngine.stopCampaign()
    }

    fun clearCallLogs() {
        viewModelScope.launch {
            autoCallRepository.deleteAllCallLogs()
        }
    }
}
