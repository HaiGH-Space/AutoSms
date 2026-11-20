package com.gmail.nthai.domain.engine

import android.content.Context
import com.gmail.nthai.domain.model.SmsTemplate
import com.gmail.nthai.domain.repository.CustomerRepository
import com.gmail.nthai.domain.repository.SendingLogRepository
import com.gmail.nthai.domain.repository.SimConfigRepository
import com.gmail.nthai.domain.repository.TemplateRepository
import com.gmail.nthai.domain.usecase.ProcessSimSendingUseCase
import com.gmail.nthai.service.SendingServiceState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsSendingEngine(
    private val context: Context?,
    private val customerRepository: CustomerRepository,
    private val templateRepository: TemplateRepository,
    private val simConfigRepository: SimConfigRepository,
    private val sendingLogRepository: SendingLogRepository,
    private val processSimSendingUseCase: ProcessSimSendingUseCase,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    @Inject
    constructor(
        @ApplicationContext context: Context?,
        customerRepository: CustomerRepository,
        templateRepository: TemplateRepository,
        simConfigRepository: SimConfigRepository,
        sendingLogRepository: SendingLogRepository,
        processSimSendingUseCase: ProcessSimSendingUseCase
    ) : this(
        context = context,
        customerRepository = customerRepository,
        templateRepository = templateRepository,
        simConfigRepository = simConfigRepository,
        sendingLogRepository = sendingLogRepository,
        processSimSendingUseCase = processSimSendingUseCase,
        externalScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    )

    private val engineScope = externalScope

    private val sendingJobs = ConcurrentHashMap<Int, Job>()
    private val pausedSlots = ConcurrentHashMap.newKeySet<Int>()
    private val stoppedSlots = ConcurrentHashMap.newKeySet<Int>()

    private val sentCounts = ConcurrentHashMap<Int, AtomicInteger>().apply {
        put(1, AtomicInteger(0))
        put(2, AtomicInteger(0))
    }
    private val totalCounts = ConcurrentHashMap<Int, AtomicInteger>().apply {
        put(1, AtomicInteger(0))
        put(2, AtomicInteger(0))
    }

    private val consecutiveFailures = ConcurrentHashMap<Int, AtomicInteger>().apply {
        put(1, AtomicInteger(0))
        put(2, AtomicInteger(0))
    }

    private val failedLogIdsBySlot = ConcurrentHashMap<Int, MutableSet<Long>>().apply {
        put(1, ConcurrentHashMap.newKeySet())
        put(2, ConcurrentHashMap.newKeySet())
    }

    private val _serviceStates = MutableStateFlow<Map<Int, SendingServiceState>>(
        mapOf(1 to SendingServiceState.Idle, 2 to SendingServiceState.Idle)
    )
    val serviceStates: StateFlow<Map<Int, SendingServiceState>> = _serviceStates.asStateFlow()

    fun startSending(simSlot: Int = 0) {
        if (simSlot in 1..2) {
            startSendingProcess(simSlot)
        } else {
            (1..2).forEach { startSendingProcess(it) }
        }
    }

    fun pauseSlot(simSlot: Int = 0) {
        controlSlots(simSlot) { slot ->
            if (sendingJobs[slot]?.isActive == true) {
                pausedSlots.add(slot)
                val sent = sentCounts[slot]?.get() ?: 0
                val total = totalCounts[slot]?.get() ?: 0
                updateSimState(
                    slot,
                    SendingServiceState.Paused(sent, total, currentFailedCount(slot))
                )
            }
        }
    }

    fun resumeSlot(simSlot: Int = 0) {
        controlSlots(simSlot) { slot ->
            if (sendingJobs[slot]?.isActive == true && pausedSlots.remove(slot)) {
                val sent = sentCounts[slot]?.get() ?: 0
                val total = totalCounts[slot]?.get() ?: 0
                updateSimState(
                    slot,
                    SendingServiceState.Running(
                        sent,
                        total,
                        "",
                        failedCount = currentFailedCount(slot)
                    )
                )
            }
        }
    }

    fun stopSlot(simSlot: Int = 0) {
        controlSlots(simSlot) { slot ->
            stoppedSlots.add(slot)
            pausedSlots.remove(slot)
            sendingJobs[slot]?.cancel()
            updateSimState(slot, SendingServiceState.Idle)
        }
    }

    fun reportSmsSuccess(simSlot: Int) {
        consecutiveFailures[simSlot]?.set(0)
    }

    fun reportSmsFailure(simSlot: Int, logId: Long) {
        val failedLogIds = failedLogIdsBySlot.computeIfAbsent(simSlot) { ConcurrentHashMap.newKeySet() }
        failedLogIds.add(logId)
        val failedCount = failedLogIds.size
        val consecutiveCount = consecutiveFailures[simSlot]?.incrementAndGet() ?: 1

        if (consecutiveCount >= MAX_CONSECUTIVE_FAILURES) {
            stoppedSlots.add(simSlot)
            pausedSlots.remove(simSlot)
            sendingJobs[simSlot]?.cancel()
            val sent = sentCounts[simSlot]?.get() ?: 0
            val total = totalCounts[simSlot]?.get() ?: 0
            val errorMsg = "Tự động dừng: Gửi thất bại 3 lần liên tiếp. Vui lòng kiểm tra lại SIM hoặc tài khoản."
            updateSimState(
                simSlot,
                SendingServiceState.Error(
                    errorMsg,
                    sent,
                    total,
                    failedCount
                )
            )
        } else {
            _serviceStates.update { states ->
                val updatedState = when (val state = states[simSlot]) {
                    is SendingServiceState.Running -> state.copy(failedCount = failedCount)
                    is SendingServiceState.Paused -> state.copy(failedCount = failedCount)
                    is SendingServiceState.Finished -> state.copy(failedCount = failedCount)
                    is SendingServiceState.Error -> state.copy(failedCount = failedCount)
                    SendingServiceState.Idle, null -> return@update states
                }
                states + (simSlot to updatedState)
            }
        }
    }

    fun isAnyJobActive(): Boolean {
        return sendingJobs.values.any { it.isActive }
    }

    fun cancelAllJobs() {
        sendingJobs.values.forEach { it.cancel() }
        sendingJobs.clear()
        pausedSlots.clear()
        stoppedSlots.clear()
    }

    fun getSentCount(simSlot: Int): Int = sentCounts[simSlot]?.get() ?: 0
    fun getTotalCount(simSlot: Int): Int = totalCounts[simSlot]?.get() ?: 0
    fun getAggregateProgress(): Pair<Int, Int> {
        val sent = sentCounts.values.sumOf { it.get() }
        val total = totalCounts.values.sumOf { it.get() }
        return sent to total
    }

    private fun controlSlots(requestedSlot: Int, action: (Int) -> Unit) {
        if (requestedSlot in 1..2) action(requestedSlot) else (1..2).forEach(action)
    }

    private fun startSendingProcess(simSlot: Int) {
        if (sendingJobs[simSlot]?.isActive == true) return
        stoppedSlots.remove(simSlot)
        pausedSlots.remove(simSlot)
        sentCounts[simSlot]?.set(0)
        totalCounts[simSlot]?.set(0)
        consecutiveFailures[simSlot]?.set(0)
        failedLogIdsBySlot[simSlot]?.clear()
        updateSimState(simSlot, SendingServiceState.Running(0, 0, "Đang chuẩn bị"))
        val sessionStartTime = System.currentTimeMillis()
        val job = engineScope.launch(start = CoroutineStart.LAZY) {
            try {
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                simConfigRepository.resetDailyCountsIfNewDay(todayDate)

                val pendingCustomers = customerRepository.getSelectedCustomersForSending().first()
                    .filter { it.sendSimSlot == simSlot }
                if (pendingCustomers.isEmpty()) {
                    updateSimState(simSlot, SendingServiceState.Finished(0, 0))
                    return@launch
                }

                val existingLogsByCustomer = pendingCustomers.associate { customer ->
                    customer.id to sendingLogRepository.getLogsForCustomer(customer.id)
                }
                val currentTotal = processSimSendingUseCase.calculateTotal(
                    pendingCustomers,
                    existingLogsByCustomer,
                    sessionStartTime
                )
                totalCounts[simSlot]?.set(currentTotal)
                sentCounts[simSlot]?.set(0)

                val template = templateRepository.getSelectedTemplate().first()
                    ?: templateRepository.getAllTemplates().first().firstOrNull()
                    ?: SmsTemplate()
                val rule = simConfigRepository.getSendingRule().first()

                val error = processSimSendingUseCase.execute(
                    context = context,
                    simSlot = simSlot,
                    customers = pendingCustomers,
                    existingLogsByCustomer = existingLogsByCustomer,
                    template = template,
                    rule = rule,
                    isPaused = { slot -> pausedSlots.contains(slot) },
                    isStopped = { slot -> stoppedSlots.contains(slot) },
                    sessionStartTime = sessionStartTime,
                    onProgressUpdate = { sent, total, displayName, secondsUntilNext ->
                        sentCounts[simSlot]?.set(sent)
                        totalCounts[simSlot]?.set(total)
                        updateSimState(
                            simSlot,
                            SendingServiceState.Running(
                                sent,
                                total,
                                displayName,
                                secondsUntilNext,
                                currentFailedCount(simSlot)
                            )
                        )
                    },
                    onReportSmsFailure = { slot, logId ->
                        reportSmsFailure(slot, logId)
                    }
                )

                if (!stoppedSlots.contains(simSlot)) {
                    val sent = sentCounts[simSlot]?.get() ?: 0
                    val total = totalCounts[simSlot]?.get() ?: 0
                    val state = error?.let {
                        SendingServiceState.Error(
                            it,
                            sent,
                            total,
                            currentFailedCount(simSlot)
                        )
                    } ?: SendingServiceState.Finished(
                        sent,
                        total,
                        currentFailedCount(simSlot)
                    )
                    updateSimState(simSlot, state)
                }
            } catch (e: Exception) {
                if (!stoppedSlots.contains(simSlot)) {
                    val errorMsg = e.message ?: "Lỗi hệ thống khi gửi SMS bằng SIM $simSlot."
                    val sent = sentCounts[simSlot]?.get() ?: 0
                    val total = totalCounts[simSlot]?.get() ?: 0
                    updateSimState(
                        simSlot,
                        SendingServiceState.Error(
                            errorMsg,
                            sent,
                            total,
                            currentFailedCount(simSlot)
                        )
                    )
                }
            } finally {
                sendingJobs.remove(simSlot)
            }
        }
        sendingJobs[simSlot] = job
        job.start()
    }

    private fun updateSimState(simSlot: Int, newState: SendingServiceState) {
        _serviceStates.update { it + (simSlot to newState) }
    }

    private fun currentFailedCount(simSlot: Int): Int =
        failedLogIdsBySlot[simSlot]?.size ?: 0

    companion object {
        const val MAX_CONSECUTIVE_FAILURES = 3
    }
}
