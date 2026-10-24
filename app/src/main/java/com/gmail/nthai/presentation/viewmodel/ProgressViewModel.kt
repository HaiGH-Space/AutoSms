package com.gmail.nthai.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.repository.SendingLogRepository
import com.gmail.nthai.service.SendingServiceState
import com.gmail.nthai.service.SmsForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    sendingLogRepository: SendingLogRepository
) : ViewModel() {

    val serviceState: StateFlow<SendingServiceState> = SmsForegroundService.serviceState

    val logs: StateFlow<List<LogRecord>> = sendingLogRepository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startSending(context: Context) {
        val intent = Intent(context, SmsForegroundService::class.java).apply {
            action = SmsForegroundService.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun pauseSending(context: Context) {
        val intent = Intent(context, SmsForegroundService::class.java).apply {
            action = SmsForegroundService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun resumeSending(context: Context) {
        val intent = Intent(context, SmsForegroundService::class.java).apply {
            action = SmsForegroundService.ACTION_RESUME
        }
        context.startService(intent)
    }

    fun stopSending(context: Context) {
        val intent = Intent(context, SmsForegroundService::class.java).apply {
            action = SmsForegroundService.ACTION_STOP
        }
        context.startService(intent)
    }
}
