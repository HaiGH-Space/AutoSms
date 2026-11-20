package com.gmail.nthai.domain.engine

import android.app.PendingIntent
import com.gmail.nthai.data.sms.SimManagerWrapper
import com.gmail.nthai.data.sms.SmsSenderEngine
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import com.gmail.nthai.domain.model.HomeDashboardStats
import com.gmail.nthai.domain.model.ImportMode
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.model.ResetStatusScope
import com.gmail.nthai.domain.model.SendingRule
import com.gmail.nthai.domain.model.SimConfig
import com.gmail.nthai.domain.model.SmsTemplate
import com.gmail.nthai.domain.repository.CustomerRepository
import com.gmail.nthai.domain.repository.SendingLogRepository
import com.gmail.nthai.domain.repository.SimConfigRepository
import com.gmail.nthai.domain.repository.TemplateRepository
import com.gmail.nthai.domain.usecase.CalculateNextDelayUseCase
import com.gmail.nthai.domain.usecase.ParseSpintaxUseCase
import com.gmail.nthai.domain.usecase.ProcessSimSendingUseCase
import com.gmail.nthai.domain.usecase.ValidateSendingConstraintUseCase
import com.gmail.nthai.service.SendingServiceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SmsSendingEngineTest {

    private val fakeCustomerRepository = FakeCustomerRepository()
    private val fakeTemplateRepository = FakeTemplateRepository()
    private val fakeSimConfigRepository = FakeSimConfigRepository()
    private val fakeSendingLogRepository = FakeSendingLogRepository()

    private val processSimSendingUseCase = ProcessSimSendingUseCase(
        customerRepository = fakeCustomerRepository,
        simConfigRepository = fakeSimConfigRepository,
        sendingLogRepository = fakeSendingLogRepository,
        parseSpintaxUseCase = ParseSpintaxUseCase(),
        validateConstraintUseCase = ValidateSendingConstraintUseCase(),
        calculateNextDelayUseCase = CalculateNextDelayUseCase(),
        simManagerWrapper = FakeSimManagerWrapper(),
        smsSenderEngine = FakeSmsSenderEngine()
    )

    @Test
    fun `initialState is Idle for both slots`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val engine = SmsSendingEngine(
            context = null,
            customerRepository = fakeCustomerRepository,
            templateRepository = fakeTemplateRepository,
            simConfigRepository = fakeSimConfigRepository,
            sendingLogRepository = fakeSendingLogRepository,
            processSimSendingUseCase = processSimSendingUseCase,
            externalScope = CoroutineScope(testDispatcher)
        )

        val states = engine.serviceStates.value
        assertEquals(SendingServiceState.Idle, states[1])
        assertEquals(SendingServiceState.Idle, states[2])
        assertFalse(engine.isAnyJobActive())
    }

    @Test
    fun `startSending when no pending customers finishes immediately`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val engine = SmsSendingEngine(
            context = null,
            customerRepository = fakeCustomerRepository,
            templateRepository = fakeTemplateRepository,
            simConfigRepository = fakeSimConfigRepository,
            sendingLogRepository = fakeSendingLogRepository,
            processSimSendingUseCase = processSimSendingUseCase,
            externalScope = CoroutineScope(testDispatcher)
        )

        engine.startSending(1)
        testScheduler.advanceUntilIdle()

        val state = engine.serviceStates.value[1]
        assertTrue("Slot 1 should be Finished with 0/0", state is SendingServiceState.Finished)
        val finishedState = state as SendingServiceState.Finished
        assertEquals(0, finishedState.sentCount)
        assertEquals(0, finishedState.totalCount)
    }

    @Test
    fun `pauseSlot and resumeSlot update serviceState correctly`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val engine = SmsSendingEngine(
            context = null,
            customerRepository = fakeCustomerRepository,
            templateRepository = fakeTemplateRepository,
            simConfigRepository = fakeSimConfigRepository,
            sendingLogRepository = fakeSendingLogRepository,
            processSimSendingUseCase = processSimSendingUseCase,
            externalScope = CoroutineScope(testDispatcher)
        )

        val customer1 = Customer(
            id = 1L,
            phone = "0901234567",
            status = CustomerStatus.PENDING,
            isSelected = true,
            sendSimSlot = 1
        )
        val customer2 = Customer(
            id = 2L,
            phone = "0907654321",
            status = CustomerStatus.PENDING,
            isSelected = true,
            sendSimSlot = 1
        )
        fakeCustomerRepository.insertCustomers(listOf(customer1, customer2))

        engine.startSending(1)
        testScheduler.runCurrent()

        engine.pauseSlot(1)
        val stateAfterPause = engine.serviceStates.value[1]
        assertTrue("State should be Paused", stateAfterPause is SendingServiceState.Paused)

        engine.resumeSlot(1)
        val stateAfterResume = engine.serviceStates.value[1]
        assertTrue("State should be Running after resume", stateAfterResume is SendingServiceState.Running)

        engine.stopSlot(1)
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun `stopSlot cancels job and sets state to Idle`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val engine = SmsSendingEngine(
            context = null,
            customerRepository = fakeCustomerRepository,
            templateRepository = fakeTemplateRepository,
            simConfigRepository = fakeSimConfigRepository,
            sendingLogRepository = fakeSendingLogRepository,
            processSimSendingUseCase = processSimSendingUseCase,
            externalScope = CoroutineScope(testDispatcher)
        )

        val customer1 = Customer(
            id = 1L,
            phone = "0901234567",
            status = CustomerStatus.PENDING,
            isSelected = true,
            sendSimSlot = 1
        )
        val customer2 = Customer(
            id = 2L,
            phone = "0907654321",
            status = CustomerStatus.PENDING,
            isSelected = true,
            sendSimSlot = 1
        )
        fakeCustomerRepository.insertCustomers(listOf(customer1, customer2))

        engine.startSending(1)
        testScheduler.runCurrent()

        engine.stopSlot(1)
        testScheduler.advanceUntilIdle()

        val state = engine.serviceStates.value[1]
        assertEquals(SendingServiceState.Idle, state)
        assertFalse(engine.isAnyJobActive())
    }

    @Test
    fun `reportSmsFailure updates failedCount in running state`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val engine = SmsSendingEngine(
            context = null,
            customerRepository = fakeCustomerRepository,
            templateRepository = fakeTemplateRepository,
            simConfigRepository = fakeSimConfigRepository,
            sendingLogRepository = fakeSendingLogRepository,
            processSimSendingUseCase = processSimSendingUseCase,
            externalScope = CoroutineScope(testDispatcher)
        )

        val customer1 = Customer(
            id = 1L,
            phone = "0901234567",
            status = CustomerStatus.PENDING,
            isSelected = true,
            sendSimSlot = 1
        )
        val customer2 = Customer(
            id = 2L,
            phone = "0907654321",
            status = CustomerStatus.PENDING,
            isSelected = true,
            sendSimSlot = 1
        )
        fakeCustomerRepository.insertCustomers(listOf(customer1, customer2))

        engine.startSending(1)
        testScheduler.runCurrent()

        engine.reportSmsFailure(1, 101L)
        val state = engine.serviceStates.value[1]
        val failedCount = when (state) {
            is SendingServiceState.Running -> state.failedCount
            is SendingServiceState.Paused -> state.failedCount
            is SendingServiceState.Finished -> state.failedCount
            is SendingServiceState.Error -> state.failedCount
            else -> 0
        }
        assertEquals(1, failedCount)

        engine.stopSlot(1)
        testScheduler.advanceUntilIdle()
    }

    private class FakeSimManagerWrapper : SimManagerWrapper(null) {
        override fun getSubscriptionIdForSlot(simSlot: Int): Int = 101
        override fun getActiveSims(): List<SimConfig> = listOf(
            SimConfig(subscriptionId = 101, simSlot = 1, carrierName = "Viettel", dailyLimit = 500)
        )
    }

    private class FakeSmsSenderEngine : SmsSenderEngine(null) {
        override fun sendSms(
            subscriptionId: Int,
            destinationAddress: String,
            text: String,
            sentIntent: PendingIntent?,
            deliveryIntent: PendingIntent?
        ) {
            // Fake send SMS without Android system framework
        }
    }

    private class FakeCustomerRepository : CustomerRepository {
        private val customers = mutableMapOf<Long, Customer>()

        override fun getAllCustomers(): Flow<List<Customer>> = flowOf(customers.values.toList())
        override fun observeDashboardStats(): Flow<HomeDashboardStats> =
            flowOf(HomeDashboardStats(0, 0, 0, 0, 0))

        override fun getSelectedCustomersForSending(): Flow<List<Customer>> =
            flowOf(customers.values.filter { it.isSelected })

        override suspend fun getCustomerById(id: Long): Customer? = customers[id]
        override suspend fun insertCustomers(customers: List<Customer>) {
            customers.forEach { this.customers[it.id] = it }
        }

        override suspend fun replaceAllCustomers(customers: List<Customer>) {
            this.customers.clear()
            insertCustomers(customers)
        }

        override suspend fun importCustomers(customers: List<Customer>, mode: ImportMode): Int {
            insertCustomers(customers)
            return customers.size
        }

        override suspend fun resetCustomerStatus(
            scope: ResetStatusScope,
            selectedCustomerIds: List<Long>
        ) {
            customers.keys.forEach { id ->
                customers[id] = customers[id]!!.copy(status = CustomerStatus.PENDING)
            }
        }

        override suspend fun updateCustomer(customer: Customer) {}
        override suspend fun updateCustomerStatus(id: Long, status: CustomerStatus) {
            customers[id] = customers[id]?.copy(status = status) ?: return
        }

        override suspend fun updateCustomerSelection(id: Long, isSelected: Boolean) {}
        override suspend fun updateCustomerSelectionForSim(
            id: Long,
            simSlot: Int,
            isSelected: Boolean
        ) {}

        override suspend fun updateAllSelection(isSelected: Boolean) {}
        override suspend fun replaceSelection(selectedIds: List<Long>) {}
        override suspend fun updateSelectionForCustomers(
            customerIds: List<Long>,
            isSelected: Boolean
        ) {}

        override suspend fun updateSelectionForSim(
            customerIds: List<Long>,
            simSlot: Int,
            isSelected: Boolean
        ) {}

        override suspend fun deleteAllCustomers() {
            customers.clear()
        }

        override suspend fun deleteCustomer(customer: Customer) {
            customers.remove(customer.id)
        }
    }

    private class FakeTemplateRepository : TemplateRepository {
        override fun getSelectedTemplate(): Flow<SmsTemplate?> = flowOf(SmsTemplate(content = "Xin chào"))
        override fun getAllTemplates(): Flow<List<SmsTemplate>> = flowOf(listOf(SmsTemplate(content = "Xin chào")))
        override fun getTemplateById(id: Long): Flow<SmsTemplate?> = flowOf(listOf(SmsTemplate(content = "Xin chào")).firstOrNull())
        override suspend fun saveTemplate(template: SmsTemplate): Long = 1L
        override suspend fun deleteTemplate(id: Long) {}
        override suspend fun setSelectedTemplate(id: Long) {}
    }

    private class FakeSendingLogRepository : SendingLogRepository {
        private val logs = mutableListOf<LogRecord>()
        override fun getAllLogs(): Flow<List<LogRecord>> = flowOf(logs)
        override suspend fun insertLog(log: LogRecord): Long = 1L
        override suspend fun updateLog(log: LogRecord) {}
        override suspend fun getLogById(id: Long): LogRecord? = null
        override suspend fun updateLogStatus(id: Long, status: LogStatus, errorMessage: String?) {}
        override suspend fun markLogSuccessIfPending(id: Long): Int = 0
        override suspend fun markLogFailedAndGetPreviousStatus(id: Long, errorMessage: String?): LogStatus? = null
        override suspend fun getLogsForCustomer(customerId: Long): List<LogRecord> = emptyList()
        override suspend fun deleteAllLogs() {}
        override suspend fun deleteLogsForCustomers(customerIds: List<Long>) {}
    }

    private class FakeSimConfigRepository : SimConfigRepository {
        override fun getSimConfigs(): Flow<List<SimConfig>> = flowOf(emptyList())
        override suspend fun getSimConfigBySlot(simSlot: Int): SimConfig = SimConfig(subscriptionId = 101, simSlot = 1, minDelaySec = 10, maxDelaySec = 10)
        override suspend fun saveSimConfig(config: SimConfig) {}
        override suspend fun incrementSentCount(simSlot: Int) {}
        override suspend fun decrementSentCount(simSlot: Int) {}
        override fun getSendingRule(): Flow<SendingRule> = flowOf(SendingRule(startHour = 0, startMinute = 0, endHour = 24, endMinute = 0))
        override suspend fun saveSendingRule(rule: SendingRule) {}
        override suspend fun resetDailyCountsIfNewDay(todayDate: String) {}
    }
}
