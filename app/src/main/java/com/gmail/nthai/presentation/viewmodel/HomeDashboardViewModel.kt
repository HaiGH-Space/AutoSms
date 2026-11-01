package com.gmail.nthai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.repository.CustomerRepository
import com.gmail.nthai.domain.repository.SendingLogRepository
import com.gmail.nthai.domain.repository.SimConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeDashboardStats(
    val totalCustomers: Int = 0,
    val sim1AssignedCount: Int = 0,
    val sim1ReadyCount: Int = 0,
    val sim2AssignedCount: Int = 0,
    val sim2ReadyCount: Int = 0,
    val sentToday: Int = 0,
    val successRateToday: Float? = null
)

@HiltViewModel
class HomeDashboardViewModel @Inject constructor(
    customerRepository: CustomerRepository,
    sendingLogRepository: SendingLogRepository,
    simConfigRepository: SimConfigRepository
) : ViewModel() {

    val stats: StateFlow<HomeDashboardStats> = combine(
        customerRepository.getAllCustomers(),
        sendingLogRepository.getAllLogs(),
        simConfigRepository.getSimConfigs()
    ) { customers, logs, simConfigs ->
        val now = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val startOfToday = now.atStartOfDay(zone).toInstant().toEpochMilli()
        val startOfTomorrow = now.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val todayLogs = logs.filter { it.timestamp >= startOfToday && it.timestamp < startOfTomorrow }
        val successfulToday = todayLogs.count { it.status == LogStatus.SENT_SUCCESS }
        val completedToday = todayLogs.count {
            it.status == LogStatus.SENT_SUCCESS || it.status == LogStatus.FAILED
        }

        fun assignedCount(simSlot: Int): Int =
            customers.count { it.isAssignedTo(simSlot) }

        fun readyCount(simSlot: Int): Int =
            customers.count { it.isAssignedTo(simSlot) && it.status == CustomerStatus.PENDING }

        HomeDashboardStats(
            totalCustomers = customers.size,
            sim1AssignedCount = assignedCount(1),
            sim1ReadyCount = readyCount(1),
            sim2AssignedCount = assignedCount(2),
            sim2ReadyCount = readyCount(2),
            sentToday = simConfigs.sumOf { it.sentCountToday },
            successRateToday = if (completedToday == 0) {
                null
            } else {
                successfulToday.toFloat() / completedToday * 100f
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeDashboardStats())
}

private fun Customer.isAssignedTo(simSlot: Int): Boolean =
    isSelected && sendSimSlot == simSlot
