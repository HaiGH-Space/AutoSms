package com.gmail.nthai.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.repository.CustomerRepository
import com.gmail.nthai.domain.repository.SendingLogRepository
import com.gmail.nthai.service.SendingServiceState
import com.gmail.nthai.service.SmsForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    customerRepository: CustomerRepository,
    sendingLogRepository: SendingLogRepository
) : ViewModel() {

    val serviceStates: StateFlow<Map<Int, SendingServiceState>> = SmsForegroundService.serviceStates

    val logs: StateFlow<List<LogRecord>> = sendingLogRepository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plannedMessageCounts: StateFlow<Map<Int, Int>> = combine(
        customerRepository.getSelectedCustomersForSending(),
        sendingLogRepository.getAllLogs()
    ) { customers, logs ->
        val latestLogs = logs
            .groupBy { it.customerId to it.phone }
            .mapValues { (_, customerLogs) -> customerLogs.maxBy { it.timestamp } }

        (1..2).associateWith { simSlot ->
            customers.filter { it.sendSimSlot == simSlot }.sumOf { customer ->
                customer.phone.split(',')
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .count { phone ->
                        val status = latestLogs[customer.id to phone]?.status
                        status != LogStatus.PENDING && status != LogStatus.SENT_SUCCESS
                    }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), mapOf(1 to 0, 2 to 0))

    fun startSending(context: Context, simSlot: Int) {
        val intent = Intent(context, SmsForegroundService::class.java).apply {
            action = SmsForegroundService.ACTION_START
            putExtra(SmsForegroundService.EXTRA_SIM_SLOT, simSlot)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun pauseSending(context: Context, simSlot: Int) {
        val intent = Intent(context, SmsForegroundService::class.java).apply {
            action = SmsForegroundService.ACTION_PAUSE
            putExtra(SmsForegroundService.EXTRA_SIM_SLOT, simSlot)
        }
        context.startService(intent)
    }

    fun resumeSending(context: Context, simSlot: Int) {
        val intent = Intent(context, SmsForegroundService::class.java).apply {
            action = SmsForegroundService.ACTION_RESUME
            putExtra(SmsForegroundService.EXTRA_SIM_SLOT, simSlot)
        }
        context.startService(intent)
    }

    fun stopSending(context: Context, simSlot: Int) {
        val intent = Intent(context, SmsForegroundService::class.java).apply {
            action = SmsForegroundService.ACTION_STOP
            putExtra(SmsForegroundService.EXTRA_SIM_SLOT, simSlot)
        }
        context.startService(intent)
    }
}
