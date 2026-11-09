package com.gmail.nthai.data.repository

import com.gmail.nthai.data.local.dao.CustomerDao
import com.gmail.nthai.data.local.mapper.toDomain
import com.gmail.nthai.data.local.mapper.toEntity
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import com.gmail.nthai.domain.model.HomeDashboardStats
import com.gmail.nthai.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.gmail.nthai.domain.model.ImportMode
import com.gmail.nthai.domain.model.ResetStatusScope

class CustomerRepositoryImpl(
    private val customerDao: CustomerDao
) : CustomerRepository {

    override fun getAllCustomers(): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { list -> list.map { it.toDomain() } }
    }

    override fun observeDashboardStats(): Flow<HomeDashboardStats> {
        return customerDao.observeDashboardStats().map { stats ->
            HomeDashboardStats(
                totalCustomers = stats.totalCustomers,
                sim1AssignedCount = stats.sim1AssignedCount,
                sim1ReadyCount = stats.sim1ReadyCount,
                sim2AssignedCount = stats.sim2AssignedCount,
                sim2ReadyCount = stats.sim2ReadyCount
            )
        }
    }

    override fun getSelectedCustomersForSending(): Flow<List<Customer>> {
        return customerDao.getSelectedCustomersForSending().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getCustomerById(id: Long): Customer? {
        return customerDao.getCustomerById(id)?.toDomain()
    }

    override suspend fun insertCustomers(customers: List<Customer>) {
        customerDao.insertCustomers(customers.map { it.toEntity() })
    }

    override suspend fun replaceAllCustomers(customers: List<Customer>) {
        customerDao.replaceAllCustomers(customers.map { it.toEntity() })
    }

    override suspend fun importCustomers(customers: List<Customer>, mode: ImportMode): Int {
        return when (mode) {
            ImportMode.REPLACE_ALL -> {
                replaceAllCustomers(customers)
                customers.size
            }
            ImportMode.APPEND -> {
                insertCustomers(customers)
                customers.size
            }
            ImportMode.SKIP_DUPLICATES -> {
                val existingPhones = HashSet<String>()
                customerDao.getAllCustomerPhones().forEach { raw ->
                    raw.split(',').map(String::trim).filter(String::isNotEmpty).forEach { existingPhones.add(it) }
                }
                val filtered = mutableListOf<Customer>()
                for (customer in customers) {
                    val customerPhones = customer.phone.split(',').map(String::trim).filter(String::isNotEmpty)
                    if (customerPhones.isNotEmpty() && customerPhones.none { existingPhones.contains(it) }) {
                        filtered.add(customer)
                        existingPhones.addAll(customerPhones)
                    }
                }
                if (filtered.isNotEmpty()) {
                    insertCustomers(filtered)
                }
                filtered.size
            }
        }
    }

    override suspend fun resetCustomerStatus(scope: ResetStatusScope, selectedCustomerIds: List<Long>) {
        when (scope) {
            ResetStatusScope.ALL -> {
                customerDao.resetAllCustomerStatus()
            }
            ResetStatusScope.SENT_ONLY -> {
                customerDao.resetCustomerStatusByStatus(CustomerStatus.SENT.name)
            }
            ResetStatusScope.FAILED_ONLY -> {
                customerDao.resetCustomerStatusByStatus(CustomerStatus.FAILED.name)
            }
            ResetStatusScope.SELECTED_ONLY -> {
                if (selectedCustomerIds.isNotEmpty()) {
                    customerDao.resetCustomerStatusByIds(selectedCustomerIds)
                }
            }
        }
    }

    override suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer.toEntity())
    }

    override suspend fun updateCustomerStatus(id: Long, status: CustomerStatus) {
        customerDao.updateCustomerStatus(id, status.name)
    }

    override suspend fun updateCustomerSelection(id: Long, isSelected: Boolean) {
        customerDao.updateCustomerSelection(id, isSelected)
    }

    override suspend fun updateCustomerSelectionForSim(id: Long, simSlot: Int, isSelected: Boolean) {
        customerDao.updateCustomerSelectionForSim(id, simSlot, isSelected)
    }

    override suspend fun updateAllSelection(isSelected: Boolean) {
        customerDao.updateAllSelection(isSelected)
    }

    override suspend fun replaceSelection(selectedIds: List<Long>) {
        customerDao.replaceSelection(selectedIds)
    }

    override suspend fun updateSelectionForCustomers(customerIds: List<Long>, isSelected: Boolean) {
        if (customerIds.isNotEmpty()) {
            customerDao.updateSelectionForCustomers(customerIds, isSelected)
        }
    }

    override suspend fun updateSelectionForSim(customerIds: List<Long>, simSlot: Int, isSelected: Boolean) {
        if (customerIds.isNotEmpty()) {
            customerDao.updateSelectionForSim(customerIds, simSlot, isSelected)
        }
    }

    override suspend fun deleteAllCustomers() {
        customerDao.deleteAllCustomers()
    }

    override suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer.toEntity())
    }
}
