package com.gmail.nthai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import com.gmail.nthai.domain.repository.CustomerRepository
import com.gmail.nthai.domain.repository.SimConfigRepository
import com.gmail.nthai.domain.usecase.DetectVietnameseCarrierUseCase
import com.gmail.nthai.domain.usecase.ImportCustomerExcelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.inject.Inject

enum class CustomerFilter {
    ALL, PENDING, SENT, FAILED
}

enum class CustomerCarrierFilter(val label: String, val carrierName: String? = null) {
    ALL("Mọi nhà mạng"),
    VIETTEL("Viettel", "Viettel"),
    VINAPHONE("VinaPhone", "VinaPhone"),
    MOBIFONE("MobiFone", "MobiFone"),
    VIETNAMOBILE("Vietnamobile", "Vietnamobile"),
    GMOBILE("Gmobile", "Gmobile"),
    WINTEL("Wintel", "Wintel"),
    ITEL("iTel", "iTel"),
    UNKNOWN("Không rõ")
}

data class PendingImportState(
    val fileBytes: ByteArray,
    val headers: List<String>,
    val selectedHeader: String
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val simConfigRepository: SimConfigRepository,
    private val importCustomerExcelUseCase: ImportCustomerExcelUseCase
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    val query: StateFlow<String> = searchQuery

    private val selectedFilter = MutableStateFlow(CustomerFilter.ALL)
    val filter: StateFlow<CustomerFilter> = selectedFilter

    private val selectedCarrierFilter = MutableStateFlow(CustomerCarrierFilter.ALL)
    val carrierFilter: StateFlow<CustomerCarrierFilter> = selectedCarrierFilter

    private val selectedSimSlot = MutableStateFlow(1)
    val simSlot: StateFlow<Int> = selectedSimSlot

    val selectedCustomerCount: StateFlow<Int> = combine(
        customerRepository.getAllCustomers(),
        selectedSimSlot
    ) { customers, simSlot ->
        customers.count { it.isSelected && it.sendSimSlot == simSlot }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _pendingImportState = MutableStateFlow<PendingImportState?>(null)
    val pendingImportState: StateFlow<PendingImportState?> = _pendingImportState.asStateFlow()

    val customersState: StateFlow<List<Customer>> = combine(
        customerRepository.getAllCustomers(),
        searchQuery,
        selectedFilter,
        selectedCarrierFilter,
        selectedSimSlot
    ) { list, query, filter, carrierFilter, simSlot ->
        val numbersAssignedToOtherSim = list.asSequence()
            .filter { it.isSelected && it.sendSimSlot != null && it.sendSimSlot != simSlot }
            .flatMap { it.phone.split(',').asSequence() }
            .map(String::trim)
            .filter(String::isNotBlank)
            .toSet()

        list.filter { customer ->
            val matchesQuery = query.isBlank() ||
                    customer.phone.contains(query) ||
                    customer.customFields.values.any { it.contains(query, ignoreCase = true) }

            val matchesFilter = when (filter) {
                CustomerFilter.ALL -> true
                CustomerFilter.PENDING -> customer.status == CustomerStatus.PENDING
                CustomerFilter.SENT -> customer.status == CustomerStatus.SENT
                CustomerFilter.FAILED -> customer.status == CustomerStatus.FAILED
            }

            val carrierName = DetectVietnameseCarrierUseCase.detect(customer.phone)
            val matchesCarrierFilter = when (carrierFilter) {
                CustomerCarrierFilter.ALL -> true
                CustomerCarrierFilter.UNKNOWN -> carrierName == null
                else -> carrierName == carrierFilter.carrierName
            }

            val recipientNumbers = customer.phone.split(',').map(String::trim).filter(String::isNotBlank)
            val overlapsOtherSim = recipientNumbers.any { it in numbersAssignedToOtherSim }
            val matchesSim = (customer.sendSimSlot == null || customer.sendSimSlot == simSlot) &&
                    !overlapsOtherSim

            matchesQuery && matchesFilter && matchesCarrierFilter && matchesSim
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateFilter(filter: CustomerFilter) {
        selectedFilter.value = filter
    }

    fun updateCarrierFilter(filter: CustomerCarrierFilter) {
        selectedCarrierFilter.value = filter
    }

    fun selectSimSlot(simSlot: Int) {
        if (simSlot in 1..2) {
            selectedSimSlot.value = simSlot
        }
    }

    fun prepareExcelImport(inputStream: InputStream, onHeaderError: (Throwable) -> Unit) {
        viewModelScope.launch {
            try {
                val bytes = inputStream.use { it.readBytes() }
                val headersResult = importCustomerExcelUseCase.getExcelHeaders(ByteArrayInputStream(bytes))
                headersResult.onSuccess { headers ->
                    if (headers.isEmpty()) {
                        onHeaderError(IllegalArgumentException("File Excel không tìm thấy dòng tiêu đề."))
                    } else {
                        val rule = simConfigRepository.getSendingRule().first()
                        val defaultHeader = rule.phoneColumnName.trim()
                        val matchedHeader = headers.find {
                            it.equals(defaultHeader, ignoreCase = true) ||
                            it.contains("sdt", ignoreCase = true) ||
                            it.contains("sđt", ignoreCase = true) ||
                            it.contains("phone", ignoreCase = true)
                        } ?: headers.first()

                        _pendingImportState.value = PendingImportState(
                            fileBytes = bytes,
                            headers = headers,
                            selectedHeader = matchedHeader
                        )
                    }
                }.onFailure { onHeaderError(it) }
            } catch (e: Exception) {
                onHeaderError(e)
            }
        }
    }

    fun updateSelectedImportHeader(header: String) {
        val current = _pendingImportState.value ?: return
        _pendingImportState.value = current.copy(selectedHeader = header)
    }

    fun cancelImport() {
        _pendingImportState.value = null
    }

    fun confirmImport(onResult: (Result<Int>) -> Unit) {
        val current = _pendingImportState.value ?: return
        _pendingImportState.value = null
        viewModelScope.launch {
            val inputStream = ByteArrayInputStream(current.fileBytes)
            val result = importCustomerExcelUseCase(inputStream, current.selectedHeader)
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

    fun toggleCustomerSelection(customer: Customer) {
        viewModelScope.launch {
            customerRepository.updateCustomerSelectionForSim(
                customer.id,
                selectedSimSlot.value,
                !customer.isSelected
            )
        }
    }

    fun selectAll(selectAll: Boolean) {
        viewModelScope.launch {
            val visibleCustomerIds = customersState.value.map { it.id }
            customerRepository.updateSelectionForSim(visibleCustomerIds, selectedSimSlot.value, selectAll)
        }
    }
}
