package com.gmail.nthai.service

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class SimSlotSendingController {

    private val sendingJobs = ConcurrentHashMap<Int, Job>()
    val pausedSlots: ConcurrentHashMap.KeySetView<Int, Boolean> = ConcurrentHashMap.newKeySet()
    val stoppedSlots: ConcurrentHashMap.KeySetView<Int, Boolean> = ConcurrentHashMap.newKeySet()

    private val _serviceStates = MutableStateFlow<Map<Int, SendingServiceState>>(emptyMap())
    val serviceStates: StateFlow<Map<Int, SendingServiceState>> = _serviceStates.asStateFlow()

    private val sentCounts = ConcurrentHashMap<Int, Int>()
    private val totalCounts = ConcurrentHashMap<Int, Int>()
    private val progressLock = Any()

    fun isSlotPaused(simSlot: Int): Boolean = pausedSlots.contains(simSlot)
    fun isSlotStopped(simSlot: Int): Boolean = stoppedSlots.contains(simSlot)

    fun registerJob(simSlot: Int, job: Job) {
        sendingJobs[simSlot] = job
    }

    fun hasActiveJobs(): Boolean {
        return sendingJobs.values.any { it.isActive }
    }

    fun setCounts(simSlot: Int, sent: Int, total: Int) {
        synchronized(progressLock) {
            sentCounts[simSlot] = sent
            totalCounts[simSlot] = total
        }
    }

    fun getCounts(simSlot: Int): Pair<Int, Int> {
        synchronized(progressLock) {
            return (sentCounts[simSlot] ?: 0) to (totalCounts[simSlot] ?: 0)
        }
    }

    fun updateSimState(simSlot: Int, state: SendingServiceState) {
        _serviceStates.value = _serviceStates.value.toMutableMap().apply {
            put(simSlot, state)
        }
    }

    fun controlSlots(targetSimSlot: Int, action: (Int) -> Unit) {
        if (targetSimSlot == 0) {
            (1..2).forEach(action)
        } else {
            action(targetSimSlot)
        }
    }

    fun pauseSlot(simSlot: Int) {
        controlSlots(simSlot) { slot ->
            pausedSlots.add(slot)
            val currentState = _serviceStates.value[slot]
            if (currentState is SendingServiceState.Running) {
                updateSimState(
                    slot,
                    SendingServiceState.Paused(
                        currentState.sentCount,
                        currentState.totalCount,
                        currentState.failedCount
                    )
                )
            }
        }
    }

    fun resumeSlot(simSlot: Int) {
        controlSlots(simSlot) { slot ->
            pausedSlots.remove(slot)
            val currentState = _serviceStates.value[slot]
            if (currentState is SendingServiceState.Paused) {
                updateSimState(
                    slot,
                    SendingServiceState.Running(
                        sentCount = currentState.sentCount,
                        totalCount = currentState.totalCount,
                        currentCustomerName = "Đang tiếp tục...",
                        failedCount = currentState.failedCount
                    )
                )
            }
        }
    }

    fun stopSlot(simSlot: Int) {
        controlSlots(simSlot) { slot ->
            stoppedSlots.add(slot)
            pausedSlots.remove(slot)
            sendingJobs[slot]?.cancel()
            updateSimState(slot, SendingServiceState.Idle)
        }
    }

    fun cancelAllJobs() {
        sendingJobs.values.forEach { it.cancel() }
        sendingJobs.clear()
        pausedSlots.clear()
        stoppedSlots.clear()
    }
}
