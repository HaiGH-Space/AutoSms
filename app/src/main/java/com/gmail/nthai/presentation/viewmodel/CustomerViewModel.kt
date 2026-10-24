package com.gmail.nthai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import com.gmail.nthai.domain.repository.CustomerRepository
import com.gmail.nthai.domain.usecase.ImportCustomerExcelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

enum class CustomerFilter {
    ALL, PENDING, SENT, FAILED
}

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val importCustomerExcelUseCase: ImportCustomerExcelUseCase
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    val query: StateFlow<String> = searchQuery

    private val selectedFilter = MutableStateFlow(CustomerFilter.ALL)
    val filter: StateFlow<CustomerFilter> = selectedFilter

    val customersState: StateFlow<List<Customer>> = combine(
        customerRepository.getAllCustomers(),
        searchQuery,
        selectedFilter
    ) { list, query, filter ->
        list.filter { customer ->
            val matchesQuery = query.isBlank() ||
                    customer.name.contains(query, ignoreCase = true) ||
                    customer.phone.contains(query) ||
                    customer.contractId.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                CustomerFilter.ALL -> true
                CustomerFilter.PENDING -> customer.status == CustomerStatus.PENDING
                CustomerFilter.SENT -> customer.status == CustomerStatus.SENT
                CustomerFilter.FAILED -> customer.status == CustomerStatus.FAILED
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateFilter(filter: CustomerFilter) {
        selectedFilter.value = filter
    }

    fun importExcel(inputStream: InputStream, onResult: (Result<Int>) -> Unit) {
        viewModelScope.launch {
            val result = importCustomerExcelUseCase(inputStream)
            result.onSuccess { importedCustomers ->
                customerRepository.insertCustomers(importedCustomers)
                onResult(Result.success(importedCustomers.size))
            }.onFailure { error ->
                onResult(Result.failure(error))
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(customer)
        }
    }

    fun deleteAllCustomers() {
        viewModelScope.launch {
            customerRepository.deleteAllCustomers()
        }
    }
}
