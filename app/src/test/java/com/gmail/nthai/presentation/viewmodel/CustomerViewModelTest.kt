package com.gmail.nthai.presentation.viewmodel

import com.gmail.nthai.data.excel.ExcelParser
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import com.gmail.nthai.domain.model.HomeDashboardStats
import com.gmail.nthai.domain.model.ImportMode
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.model.ResetStatusScope
import com.gmail.nthai.domain.model.SendingRule
import com.gmail.nthai.domain.model.SimConfig
import com.gmail.nthai.domain.repository.CustomerRepository
import com.gmail.nthai.domain.repository.SendingLogRepository
import com.gmail.nthai.domain.repository.SimConfigRepository
import com.gmail.nthai.domain.usecase.ImportCustomerExcelUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CustomerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeCustomerRepository = FakeCustomerRepositoryForVM()
    private val fakeSimConfigRepository = FakeSimConfigRepositoryForVM()
    private val fakeSendingLogRepository = FakeSendingLogRepositoryForVM()
    private val importCustomerExcelUseCase = ImportCustomerExcelUseCase(ExcelParser())

    private lateinit var viewModel: CustomerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CustomerViewModel(
            customerRepository = fakeCustomerRepository,
            simConfigRepository = fakeSimConfigRepository,
            sendingLogRepository = fakeSendingLogRepository,
            importCustomerExcelUseCase = importCustomerExcelUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        assertEquals("", viewModel.query.value)
        assertEquals(CustomerFilter.ALL, viewModel.filter.value)
        assertEquals(CustomerCarrierFilter.ALL, viewModel.carrierFilter.value)
        assertEquals(1, viewModel.simSlot.value)
        assertFalse(viewModel.isImporting.value)
        assertEquals(0, viewModel.selectedCustomerCount.value)
    }

    @Test
    fun testSearchQueryFilter() = runTest(testDispatcher) {
        val c1 = Customer(id = 1, phone = "0987654321", customFields = mapOf("ten_khach" to "Nguyen Van A"))
        val c2 = Customer(id = 2, phone = "0912345678", customFields = mapOf("ten_khach" to "Tran Thi B"))
        fakeCustomerRepository.setCustomers(listOf(c1, c2))

        backgroundScope.launch {
            viewModel.customersState.collect {}
        }

        viewModel.updateSearchQuery("0987")

        val filtered = withContext(Dispatchers.Default) {
            viewModel.customersState.first { it.size == 1 }
        }
        assertEquals(1, filtered.size)
        assertEquals("0987654321", filtered[0].phone)
    }

    @Test
    fun testCustomerStatusFilter() = runTest(testDispatcher) {
        val c1 = Customer(id = 1, phone = "0987654321", status = CustomerStatus.PENDING)
        val c2 = Customer(id = 2, phone = "0912345678", status = CustomerStatus.SENT)
        fakeCustomerRepository.setCustomers(listOf(c1, c2))

        backgroundScope.launch {
            viewModel.customersState.collect {}
        }

        viewModel.updateFilter(CustomerFilter.SENT)

        val filtered = withContext(Dispatchers.Default) {
            viewModel.customersState.first { it.size == 1 }
        }
        assertEquals(1, filtered.size)
        assertEquals("0912345678", filtered[0].phone)
    }

    @Test
    fun testCarrierFilter() = runTest(testDispatcher) {
        val c1 = Customer(id = 1, phone = "0987654321")
        val c2 = Customer(id = 2, phone = "0912345678")
        fakeCustomerRepository.setCustomers(listOf(c1, c2))

        backgroundScope.launch {
            viewModel.customersState.collect {}
        }

        viewModel.updateCarrierFilter(CustomerCarrierFilter.VIETTEL)

        val filtered = withContext(Dispatchers.Default) {
            viewModel.customersState.first { it.size == 1 }
        }
        assertEquals(1, filtered.size)
        assertEquals("0987654321", filtered[0].phone)
    }

    @Test
    fun testSelectSimSlot() {
        viewModel.selectSimSlot(2)
        assertEquals(2, viewModel.simSlot.value)

        // Invalid slot should be ignored
        viewModel.selectSimSlot(5)
        assertEquals(2, viewModel.simSlot.value)
    }

    @Test
    fun testDeleteCustomer() = runTest(testDispatcher) {
        val c1 = Customer(id = 1, phone = "0987654321")
        fakeCustomerRepository.setCustomers(listOf(c1))

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteCustomer(c1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeCustomerRepository.customers.value.isEmpty())
    }

    @Test
    fun testDeleteAllCustomers() = runTest(testDispatcher) {
        val c1 = Customer(id = 1, phone = "0987654321")
        val c2 = Customer(id = 2, phone = "0912345678")
        fakeCustomerRepository.setCustomers(listOf(c1, c2))

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteAllCustomers()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeCustomerRepository.customers.value.isEmpty())
    }
}

private class FakeCustomerRepositoryForVM : CustomerRepository {
    val customers = MutableStateFlow<List<Customer>>(emptyList())

    fun setCustomers(list: List<Customer>) {
        customers.value = list
    }

    override fun getAllCustomers(): Flow<List<Customer>> = customers

    override fun observeDashboardStats(): Flow<HomeDashboardStats> =
        MutableStateFlow(HomeDashboardStats(0, 0, 0, 0))

    override fun getSelectedCustomersForSending(): Flow<List<Customer>> =
        MutableStateFlow(customers.value.filter { it.isSelected })

    override suspend fun getCustomerById(id: Long): Customer? = customers.value.find { it.id == id }

    override suspend fun insertCustomers(customers: List<Customer>) {
        this.customers.value = this.customers.value + customers
    }

    override suspend fun replaceAllCustomers(customers: List<Customer>) {
        this.customers.value = customers
    }

    override suspend fun importCustomers(customers: List<Customer>, mode: ImportMode): Int {
        this.customers.value = this.customers.value + customers
        return customers.size
    }

    override suspend fun resetCustomerStatus(scope: ResetStatusScope, selectedCustomerIds: List<Long>) {
        customers.value = customers.value.map { c ->
            when (scope) {
                ResetStatusScope.ALL -> c.copy(status = CustomerStatus.PENDING)
                ResetStatusScope.SENT_ONLY -> if (c.status == CustomerStatus.SENT) c.copy(status = CustomerStatus.PENDING) else c
                ResetStatusScope.FAILED_ONLY -> if (c.status == CustomerStatus.FAILED) c.copy(status = CustomerStatus.PENDING) else c
                ResetStatusScope.SELECTED_ONLY -> if (selectedCustomerIds.contains(c.id)) c.copy(status = CustomerStatus.PENDING) else c
            }
        }
    }

    override suspend fun updateCustomer(customer: Customer) {
        customers.value = customers.value.map { if (it.id == customer.id) customer else it }
    }

    override suspend fun updateCustomerStatus(id: Long, status: CustomerStatus) {
        customers.value = customers.value.map { if (it.id == id) it.copy(status = status) else it }
    }

    override suspend fun updateCustomerSelection(id: Long, isSelected: Boolean) {
        customers.value = customers.value.map { if (it.id == id) it.copy(isSelected = isSelected) else it }
    }

    override suspend fun updateCustomerSelectionForSim(id: Long, simSlot: Int, isSelected: Boolean) {
        customers.value = customers.value.map { if (it.id == id) it.copy(isSelected = isSelected, sendSimSlot = if (isSelected) simSlot else null) else it }
    }

    override suspend fun updateAllSelection(isSelected: Boolean) {
        customers.value = customers.value.map { it.copy(isSelected = isSelected) }
    }

    override suspend fun replaceSelection(selectedIds: List<Long>) {
        customers.value = customers.value.map { it.copy(isSelected = selectedIds.contains(it.id)) }
    }

    override suspend fun updateSelectionForCustomers(customerIds: List<Long>, isSelected: Boolean) {
        customers.value = customers.value.map { if (customerIds.contains(it.id)) it.copy(isSelected = isSelected) else it }
    }

    override suspend fun updateSelectionForSim(customerIds: List<Long>, simSlot: Int, isSelected: Boolean) {
        customers.value = customers.value.map {
            if (customerIds.contains(it.id)) it.copy(isSelected = isSelected, sendSimSlot = if (isSelected) simSlot else null) else it
        }
    }

    override suspend fun deleteAllCustomers() {
        customers.value = emptyList()
    }

    override suspend fun deleteCustomer(customer: Customer) {
        customers.value = customers.value.filterNot { it.id == customer.id }
    }
}

private class FakeSimConfigRepositoryForVM : SimConfigRepository {
    private val configs = MutableStateFlow(
        listOf(
            SimConfig(simSlot = 1, subscriptionId = 1, carrierName = "Viettel"),
            SimConfig(simSlot = 2, subscriptionId = 2, carrierName = "VinaPhone")
        )
    )
    private val rule = MutableStateFlow(SendingRule())

    override fun getSimConfigs(): Flow<List<SimConfig>> = configs

    override suspend fun getSimConfigBySlot(simSlot: Int): SimConfig? =
        configs.value.find { it.simSlot == simSlot }

    override suspend fun saveSimConfig(config: SimConfig) {
        configs.value = configs.value.map { if (it.simSlot == config.simSlot) config else it }
    }

    override suspend fun incrementSentCount(simSlot: Int) {}
    override suspend fun decrementSentCount(simSlot: Int) {}
    override suspend fun resetDailyCountsIfNewDay(todayDate: String) {}

    override fun getSendingRule(): Flow<SendingRule> = rule

    override suspend fun saveSendingRule(rule: SendingRule) {
        this.rule.value = rule
    }
}

private class FakeSendingLogRepositoryForVM : SendingLogRepository {
    private val logs = MutableStateFlow<List<LogRecord>>(emptyList())

    override fun getAllLogs(): Flow<List<LogRecord>> = logs

    override suspend fun insertLog(log: LogRecord): Long = 1L
    override suspend fun updateLog(log: LogRecord) {}
    override suspend fun updateLogStatus(id: Long, status: LogStatus, errorMessage: String?) {}
    override suspend fun markLogSuccessIfPending(id: Long): Int = 1
    override suspend fun markLogFailedAndGetPreviousStatus(id: Long, errorMessage: String?): LogStatus? = null
    override suspend fun getLogById(id: Long): LogRecord? = null
    override suspend fun getLogsForCustomer(customerId: Long): List<LogRecord> = emptyList()

    override suspend fun deleteAllLogs() {
        logs.value = emptyList()
    }

    override suspend fun deleteLogsForCustomers(customerIds: List<Long>) {
        logs.value = logs.value.filterNot { customerIds.contains(it.customerId) }
    }
}
