package com.gmail.nthai.domain.repository

import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getAllCustomers(): Flow<List<Customer>>
    fun getSelectedCustomersForSending(): Flow<List<Customer>>
    suspend fun getCustomerById(id: Long): Customer?
    suspend fun insertCustomers(customers: List<Customer>)
    suspend fun updateCustomer(customer: Customer)
    suspend fun updateCustomerStatus(id: Long, status: CustomerStatus)
    suspend fun updateCustomerSelection(id: Long, isSelected: Boolean)
    suspend fun updateCustomerSelectionForSim(id: Long, simSlot: Int, isSelected: Boolean)
    suspend fun updateAllSelection(isSelected: Boolean)
    suspend fun replaceSelection(selectedIds: List<Long>)
    suspend fun updateSelectionForCustomers(customerIds: List<Long>, isSelected: Boolean)
    suspend fun updateSelectionForSim(customerIds: List<Long>, simSlot: Int, isSelected: Boolean)
    suspend fun deleteAllCustomers()
    suspend fun deleteCustomer(customer: Customer)
}
