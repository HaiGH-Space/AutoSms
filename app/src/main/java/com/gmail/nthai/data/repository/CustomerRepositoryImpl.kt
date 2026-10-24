package com.gmail.nthai.data.repository

import com.gmail.nthai.data.local.dao.CustomerDao
import com.gmail.nthai.data.local.mapper.toDomain
import com.gmail.nthai.data.local.mapper.toEntity
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import com.gmail.nthai.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CustomerRepositoryImpl(
    private val customerDao: CustomerDao
) : CustomerRepository {

    override fun getAllCustomers(): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { list -> list.map { it.toDomain() } }
    }

    override fun getPendingCustomers(): Flow<List<Customer>> {
        return customerDao.getPendingCustomers().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getCustomerById(id: Long): Customer? {
        return customerDao.getCustomerById(id)?.toDomain()
    }

    override suspend fun insertCustomers(customers: List<Customer>) {
        customerDao.insertCustomers(customers.map { it.toEntity() })
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

    override suspend fun updateAllSelection(isSelected: Boolean) {
        customerDao.updateAllSelection(isSelected)
    }

    override suspend fun deleteAllCustomers() {
        customerDao.deleteAllCustomers()
    }

    override suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer.toEntity())
    }
}
