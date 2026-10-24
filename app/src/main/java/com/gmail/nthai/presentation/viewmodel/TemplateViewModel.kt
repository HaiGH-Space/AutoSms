package com.gmail.nthai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.SmsTemplate
import com.gmail.nthai.domain.repository.CustomerRepository
import com.gmail.nthai.domain.repository.TemplateRepository
import com.gmail.nthai.domain.usecase.ParseSpintaxUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplateViewModel @Inject constructor(
    private val templateRepository: TemplateRepository,
    private val parseSpintaxUseCase: ParseSpintaxUseCase,
    customerRepository: CustomerRepository
) : ViewModel() {

    private val _template = MutableStateFlow(SmsTemplate())
    val template: StateFlow<SmsTemplate> = _template.asStateFlow()

    private val _preview = MutableStateFlow("")
    val preview: StateFlow<String> = _preview.asStateFlow()
    private val _availableFields = MutableStateFlow<List<String>>(emptyList())
    val availableFields: StateFlow<List<String>> = _availableFields.asStateFlow()

    private val sampleCustomer = Customer(
        phone = "0987654321",
        customFields = mapOf(
            "Tên khách hàng" to "Nguyễn Văn A",
            "Mã hợp đồng" to "HD001",
            "Số tiền" to "1500000",
            "Ngày hạn" to "20/10/2025"
        )
    )

    private var previewCustomer = sampleCustomer

    init {
        viewModelScope.launch {
            templateRepository.getTemplate().collectLatest { savedTemplate ->
                if (savedTemplate != null) {
                    _template.value = savedTemplate
                    generateRandomPreview()
                }
            }
        }
        viewModelScope.launch {
            customerRepository.getAllCustomers().collectLatest { customers ->
                _availableFields.value = customers
                    .flatMap { it.customFields.keys }
                    .distinct()
                previewCustomer = customers.firstOrNull() ?: sampleCustomer
                generateRandomPreview()
            }
        }
    }

    fun updateTitle(title: String) {
        _template.value = _template.value.copy(title = title)
    }

    fun updateContent(content: String) {
        _template.value = _template.value.copy(content = content)
        generateRandomPreview()
    }

    fun generateRandomPreview() {
        _preview.value = parseSpintaxUseCase(_template.value.content, previewCustomer)
    }

    fun saveTemplate(onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            templateRepository.saveTemplate(_template.value)
            onSaved()
        }
    }
}
