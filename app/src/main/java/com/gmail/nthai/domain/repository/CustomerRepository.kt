package com.gmail.nthai.domain.repository

import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getAllCustomers(): Flow<List<Customer>>
    fun getPendingCustomers(): Flow<List<Customer>>
    suspend fun getCustomerById(id: Long): Customer?
    suspend fun insertCustomers(customers: List<Customer>)
    suspend fun updateCustomer(customer: Customer)
    suspend fun updateCustomerStatus(id: Long, status: CustomerStatus)
    suspend fun deleteAllCustomers()
    suspend fun deleteCustomer(customer: Customer)
}
