package com.gmail.nthai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import com.gmail.nthai.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeDashboardStats(
    val totalCustomers: Int = 0,
    val sim1AssignedCount: Int = 0,
    val sim1ReadyCount: Int = 0,
    val sim2AssignedCount: Int = 0,
    val sim2ReadyCount: Int = 0
)

@HiltViewModel
class HomeDashboardViewModel @Inject constructor(
    customerRepository: CustomerRepository
) : ViewModel() {

    val stats: StateFlow<HomeDashboardStats> = customerRepository.getAllCustomers()
        .map { customers ->
            fun assignedCount(simSlot: Int): Int =
                customers.count { it.isAssignedTo(simSlot) }

            fun readyCount(simSlot: Int): Int =
                customers.count { it.isAssignedTo(simSlot) && it.status == CustomerStatus.PENDING }

            HomeDashboardStats(
                totalCustomers = customers.size,
                sim1AssignedCount = assignedCount(1),
                sim1ReadyCount = readyCount(1),
                sim2AssignedCount = assignedCount(2),
                sim2ReadyCount = readyCount(2)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeDashboardStats())
}

private fun Customer.isAssignedTo(simSlot: Int): Boolean =
    isSelected && sendSimSlot == simSlot
