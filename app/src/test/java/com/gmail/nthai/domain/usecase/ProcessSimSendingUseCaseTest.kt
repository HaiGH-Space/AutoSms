package com.gmail.nthai.domain.usecase

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessSimSendingUseCaseTest {

    private val fakeCustomerRepository = FakeCustomerRepository()
    private val fakeSimConfigRepository = FakeSimConfigRepository()
    private val fakeSendingLogRepository = FakeSendingLogRepository()
    private val parseSpintaxUseCase = ParseSpintaxUseCase()
    private val validateConstraintUseCase = ValidateSendingConstraintUseCase()
    private val calculateNextDelayUseCase = CalculateNextDelayUseCase()

    private val dummySimManagerWrapper = SimManagerWrapper(null)
    private val dummySmsSenderEngine = SmsSenderEngine(null)

    private val useCase = ProcessSimSendingUseCase(
        customerRepository = fakeCustomerRepository,
        simConfigRepository = fakeSimConfigRepository,
        sendingLogRepository = fakeSendingLogRepository,
        parseSpintaxUseCase = parseSpintaxUseCase,
        validateConstraintUseCase = validateConstraintUseCase,
        calculateNextDelayUseCase = calculateNextDelayUseCase,
        simManagerWrapper = dummySimManagerWrapper,
        smsSenderEngine = dummySmsSenderEngine
    )

    @Test
    fun `recordFailedLogsForSim skips SENT_SUCCESS and PENDING logs`() = runTest {
        val sessionStartTime = 5000L
        val customer = Customer(
            id = 101L,
            phone = "0901111111,0902222222",
            status = CustomerStatus.PENDING,
            isSelected = true,
            sendSimSlot = 1
        )
        fakeCustomerRepository.insertCustomers(listOf(customer))

        val successLog = LogRecord(
            id = 1L,
            customerId = customer.id,
            customerName = "Test",
            phone = "0901111111",
            messageContent = "Msg",
            simSlot = 1,
            timestamp = 1000L,
            status = LogStatus.SENT_SUCCESS
        )
        val pendingLog = LogRecord(
            id = 2L,
            customerId = customer.id,
            customerName = "Test",
            phone = "0902222222",
            messageContent = "Msg",
            simSlot = 1,
            timestamp = 6000L,
            status = LogStatus.PENDING
        )
        fakeSendingLogRepository.insertLog(successLog)
        fakeSendingLogRepository.insertLog(pendingLog)

        useCase.recordFailedLogsForSim(
            simSlot = 1,
            customers = listOf(customer),
            existingLogsByCustomer = mapOf(customer.id to listOf(successLog, pendingLog)),
            template = SmsTemplate(content = "Template"),
            errorMessage = "Error",
            sessionStartTime = sessionStartTime,
            onReportSmsFailure = { _, _ -> }
        )

        val logs = fakeSendingLogRepository.getLogsForCustomer(customer.id)
        val failedLogs = logs.filter { it.status == LogStatus.FAILED }
        assertTrue("No failed logs should be recorded for SENT_SUCCESS or PENDING recipients", failedLogs.isEmpty())
    }

    @Test
    fun `retry FAILED customer skips SENT_SUCCESS from previous session and calculates correct total`() = runTest {
        val customer = Customer(
            id = 101L,
            phone = "0901111111,0902222222",
            status = CustomerStatus.PENDING, // Status reset to PENDING as in FAILED_ONLY reset
            isSelected = true,
            sendSimSlot = 1
        )
        fakeCustomerRepository.insertCustomers(listOf(customer))

        val oldSuccessLog = LogRecord(
            id = 1L,
            customerId = customer.id,
            customerName = "Test",
            phone = "0901111111",
            messageContent = "Msg",
            simSlot = 1,
            timestamp = 1000L,
            status = LogStatus.SENT_SUCCESS
        )
        fakeSendingLogRepository.insertLog(oldSuccessLog)

        val existingLogsByCustomer = mapOf(customer.id to fakeSendingLogRepository.getLogsForCustomer(customer.id))

        val calculatedTotal = useCase.calculateTotal(listOf(customer), existingLogsByCustomer)

        assertEquals("Total should be 1 because 0901 already succeeded previously", 1, calculatedTotal)
    }

    @Test
    fun `calculateTotal includes stale PENDING log from previous session`() = runTest {
        val sessionStartTime = 5000L
        val customer = Customer(
            id = 101L,
            phone = "0901111111",
            status = CustomerStatus.PENDING,
            isSelected = true,
            sendSimSlot = 1
        )

        val oldPendingLog = LogRecord(
            id = 1L,
            customerId = customer.id,
            customerName = "Test",
            phone = "0901111111",
            messageContent = "Msg",
            simSlot = 1,
            timestamp = 1000L,
            status = LogStatus.PENDING
        )

        val existingLogsByCustomer = mapOf(customer.id to listOf(oldPendingLog))

        val calculatedTotal = useCase.calculateTotal(listOf(customer), existingLogsByCustomer, sessionStartTime)

        assertEquals("Stale PENDING log from previous session should be counted for retry", 1, calculatedTotal)
    }

    @Test
    fun `calculateTotal excludes current session PENDING log`() = runTest {
        val sessionStartTime = 5000L
        val customer = Customer(
            id = 101L,
            phone = "0901111111",
            status = CustomerStatus.PENDING,
            isSelected = true,
            sendSimSlot = 1
        )

        val currentPendingLog = LogRecord(
            id = 1L,
            customerId = customer.id,
            customerName = "Test",
            phone = "0901111111",
            messageContent = "Msg",
            simSlot = 1,
            timestamp = 6000L,
            status = LogStatus.PENDING
        )

        val existingLogsByCustomer = mapOf(customer.id to listOf(currentPendingLog))

        val calculatedTotal = useCase.calculateTotal(listOf(customer), existingLogsByCustomer, sessionStartTime)

        assertEquals("Current session PENDING log should not be counted again", 0, calculatedTotal)
    }

    private class FakeCustomerRepository : CustomerRepository {
        private val customers = mutableMapOf<Long, Customer>()

        override fun getAllCustomers(): Flow<List<Customer>> = flowOf(customers.values.toList())
        override fun observeDashboardStats(): Flow<HomeDashboardStats> = flowOf(HomeDashboardStats(0, 0, 0, 0, 0))
        override fun getSelectedCustomersForSending(): Flow<List<Customer>> = flowOf(customers.values.filter { it.isSelected })
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
        override suspend fun resetCustomerStatus(scope: ResetStatusScope, selectedCustomerIds: List<Long>) {
            customers.keys.forEach { id ->
                customers[id] = customers[id]!!.copy(status = CustomerStatus.PENDING)
            }
        }
        override suspend fun updateCustomer(customer: Customer) { customers[customer.id] = customer }
        override suspend fun updateCustomerStatus(id: Long, status: CustomerStatus) {
            customers[id] = customers[id]?.copy(status = status) ?: return
        }
        override suspend fun updateCustomerSelection(id: Long, isSelected: Boolean) {}
        override suspend fun updateCustomerSelectionForSim(id: Long, simSlot: Int, isSelected: Boolean) {}
        override suspend fun updateAllSelection(isSelected: Boolean) {}
        override suspend fun replaceSelection(selectedIds: List<Long>) {}
        override suspend fun updateSelectionForCustomers(customerIds: List<Long>, isSelected: Boolean) {}
        override suspend fun updateSelectionForSim(customerIds: List<Long>, simSlot: Int, isSelected: Boolean) {}
        override suspend fun deleteAllCustomers() { customers.clear() }
        override suspend fun deleteCustomer(customer: Customer) { customers.remove(customer.id) }
    }

    private class FakeSendingLogRepository : SendingLogRepository {
        private val logs = mutableListOf<LogRecord>()
        private var idCounter = 100L

        override fun getAllLogs(): Flow<List<LogRecord>> = flowOf(logs)
        override suspend fun insertLog(log: LogRecord): Long {
            val id = if (log.id == 0L) idCounter++ else log.id
            val newLog = log.copy(id = id)
            logs.add(newLog)
            return id
        }
        override suspend fun updateLog(log: LogRecord) {
            logs.removeAll { it.id == log.id }
            logs.add(log)
        }
        override suspend fun getLogById(id: Long): LogRecord? = logs.find { it.id == id }
        override suspend fun updateLogStatus(id: Long, status: LogStatus, errorMessage: String?) {
            val log = logs.find { it.id == id } ?: return
            updateLog(log.copy(status = status, errorMessage = errorMessage))
        }
        override suspend fun markLogSuccessIfPending(id: Long): Int = 0
        override suspend fun markLogFailedAndGetPreviousStatus(id: Long, errorMessage: String?): LogStatus? = null
        override suspend fun getLogsForCustomer(customerId: Long): List<LogRecord> {
            return logs.filter { it.customerId == customerId }
        }
        override suspend fun deleteAllLogs() { logs.clear() }
        override suspend fun deleteLogsForCustomers(customerIds: List<Long>) {
            logs.removeAll { customerIds.contains(it.customerId) }
        }
    }

    private class FakeSimConfigRepository : SimConfigRepository {
        override fun getSimConfigs(): Flow<List<SimConfig>> = flowOf(emptyList())
        override suspend fun getSimConfigBySlot(simSlot: Int): SimConfig? = null
        override suspend fun saveSimConfig(simConfig: SimConfig) {}
        override suspend fun incrementSentCount(simSlot: Int) {}
        override suspend fun decrementSentCount(simSlot: Int) {}
        override fun getSendingRule(): Flow<SendingRule> = flowOf(SendingRule())
        override suspend fun saveSendingRule(rule: SendingRule) {}
        override suspend fun resetDailyCountsIfNewDay(todayDate: String) {}
    }
}
