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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        var count = 0
        for (i in 0 until customers.size) {
            val c = customers[i]
            if (c.isSelected && c.sendSimSlot == simSlot) {
                count++
            }
        }
        count
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _pendingImportState = MutableStateFlow<PendingImportState?>(null)
    val pendingImportState: StateFlow<PendingImportState?> = _pendingImportState.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importLoadingMessage = MutableStateFlow("")
    val importLoadingMessage: StateFlow<String> = _importLoadingMessage.asStateFlow()

    val customersState: StateFlow<List<Customer>> = combine(
        customerRepository.getAllCustomers(),
        searchQuery,
        selectedFilter,
        selectedCarrierFilter,
        selectedSimSlot
    ) { list, query, filter, carrierFilter, simSlot ->
        if (list.isEmpty()) return@combine emptyList()

        val numbersAssignedToOtherSim = HashSet<String>()
        for (i in 0 until list.size) {
            val customer = list[i]
            if (customer.isSelected && customer.sendSimSlot != null && customer.sendSimSlot != simSlot) {
                val phones = customer.phone.split(',')
                for (j in 0 until phones.size) {
                    val trimmed = phones[j].trim()
                    if (trimmed.isNotEmpty()) {
                        numbersAssignedToOtherSim.add(trimmed)
                    }
                }
            }
        }

        val hasQuery = query.isNotBlank()
        val trimmedQuery = if (hasQuery) query.trim() else ""

        list.filter { customer ->
            val matchesQuery = if (!hasQuery) true else {
                customer.phone.contains(trimmedQuery, ignoreCase = true) ||
                        customer.customFields.values.any { it.contains(trimmedQuery, ignoreCase = true) }
            }
            if (!matchesQuery) return@filter false

            val matchesFilter = when (filter) {
                CustomerFilter.ALL -> true
                CustomerFilter.PENDING -> customer.status == CustomerStatus.PENDING
                CustomerFilter.SENT -> customer.status == CustomerStatus.SENT
                CustomerFilter.FAILED -> customer.status == CustomerStatus.FAILED
            }
            if (!matchesFilter) return@filter false

            val carrierName = DetectVietnameseCarrierUseCase.detect(customer.phone)
            val matchesCarrierFilter = when (carrierFilter) {
                CustomerCarrierFilter.ALL -> true
                CustomerCarrierFilter.UNKNOWN -> carrierName == null
                else -> carrierName == carrierFilter.carrierName
            }
            if (!matchesCarrierFilter) return@filter false

            val matchesSim = if (customer.sendSimSlot != null && customer.sendSimSlot != simSlot) {
                false
            } else if (numbersAssignedToOtherSim.isNotEmpty()) {
                val recipientNumbers = customer.phone.split(',')
                var overlaps = false
                for (k in 0 until recipientNumbers.size) {
                    if (recipientNumbers[k].trim() in numbersAssignedToOtherSim) {
                        overlaps = true
                        break
                    }
                }
                !overlaps
            } else {
                true
            }

            matchesSim
        }
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
            _isImporting.value = true
            _importLoadingMessage.value = "Đang đọc file Excel..."
            try {
                val (bytes, headersResult) = withContext(Dispatchers.IO) {
                    val b = inputStream.use { it.readBytes() }
                    _importLoadingMessage.value = "Đang phân tích các cột trong file..."
                    val h = importCustomerExcelUseCase.getExcelHeaders(ByteArrayInputStream(b))
                    Pair(b, h)
                }
                _isImporting.value = false
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
                _isImporting.value = false
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
            _isImporting.value = true
            _importLoadingMessage.value = "Đang phân tích và nhập danh sách khách hàng..."
            try {
                val result = withContext(Dispatchers.IO) {
                    val inputStream = ByteArrayInputStream(current.fileBytes)
                    val parseResult = importCustomerExcelUseCase(inputStream, current.selectedHeader)
                    parseResult.map { importedCustomers ->
                        _importLoadingMessage.value = "Đang lưu ${importedCustomers.size} khách hàng vào cơ sở dữ liệu..."
                        customerRepository.insertCustomers(importedCustomers)
                        importedCustomers.size
                    }
                }
                _isImporting.value = false
                result.onSuccess { count ->
                    onResult(Result.success(count))
                }.onFailure { error ->
                    onResult(Result.failure(error))
                }
            } catch (error: Exception) {
                _isImporting.value = false
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
