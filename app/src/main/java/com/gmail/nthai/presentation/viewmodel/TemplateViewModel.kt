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

    private val _allTemplates = MutableStateFlow<List<SmsTemplate>>(emptyList())
    val allTemplates: StateFlow<List<SmsTemplate>> = _allTemplates.asStateFlow()

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
    private var isInitialLoaded = false

    init {
        viewModelScope.launch {
            templateRepository.getAllTemplates().collectLatest { list ->
                if (list.isEmpty()) {
                    // Create default initial template if DB is completely empty
                    val defaultTemplate = SmsTemplate(
                        id = 0,
                        title = "Mẫu tin nhắn nhắc nợ",
                        content = "Kính gửi {{Tên khách hàng}}, hợp đồng {{Mã hợp đồng}} số tiền {{Số tiền}} đ hạn {{Ngày hạn}}. {Xin vui lòng thanh toán|Vui lòng thanh toán sớm}.",
                        isSelected = true,
                        updatedAt = System.currentTimeMillis()
                    )
                    val newId = templateRepository.saveTemplate(defaultTemplate)
                    templateRepository.setSelectedTemplate(newId)
                } else {
                    _allTemplates.value = list
                    if (!isInitialLoaded) {
                        isInitialLoaded = true
                        val selected = list.firstOrNull { it.isSelected } ?: list.first()
                        _template.value = selected
                        generateRandomPreview()
                    } else {
                        // Update currently edited template reference if it was modified
                        val updatedCurrent = list.firstOrNull { it.id == _template.value.id }
                        if (updatedCurrent != null && updatedCurrent != _template.value) {
                            _template.value = updatedCurrent
                            generateRandomPreview()
                        }
                    }
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

    fun selectTemplateToEdit(selected: SmsTemplate) {
        _template.value = selected
        generateRandomPreview()
    }

    fun createNewTemplate() {
        val newTemplate = SmsTemplate(
            id = 0,
            title = "Mẫu tin mới",
            content = "",
            isSelected = false,
            updatedAt = System.currentTimeMillis()
        )
        _template.value = newTemplate
        generateRandomPreview()
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

    fun saveTemplate(onSaved: (String) -> Unit = {}) {
        viewModelScope.launch {
            val isNew = _template.value.id == 0L
            val toSave = _template.value.copy(updatedAt = System.currentTimeMillis())
            val savedId = templateRepository.saveTemplate(toSave)
            
            val updatedTemplate = toSave.copy(id = savedId)
            _template.value = updatedTemplate

            if (isNew || _allTemplates.value.none { it.isSelected }) {
                templateRepository.setSelectedTemplate(savedId)
            }

            onSaved(if (isNew) "Đã tạo và lưu mẫu tin mới!" else "Đã cập nhật mẫu tin nhắn!")
        }
    }

    fun saveAsNewTemplate(onSaved: (String) -> Unit = {}) {
        viewModelScope.launch {
            val newTitle = if (_template.value.title.endsWith("(Bản sao)")) {
                _template.value.title
            } else {
                "${_template.value.title} (Bản sao)"
            }
            val newTemplate = _template.value.copy(
                id = 0,
                title = newTitle,
                isSelected = false,
                updatedAt = System.currentTimeMillis()
            )
            val newId = templateRepository.saveTemplate(newTemplate)
            _template.value = newTemplate.copy(id = newId)
            onSaved("Đã lưu thành mẫu tin mới!")
        }
    }

    fun setSelectedTemplate(id: Long, onSelected: () -> Unit = {}) {
        viewModelScope.launch {
            templateRepository.setSelectedTemplate(id)
            _template.value = _template.value.copy(isSelected = true)
            onSelected()
        }
    }

    fun deleteTemplate(id: Long, onDelete: () -> Unit = {}) {
        viewModelScope.launch {
            templateRepository.deleteTemplate(id)
            val remaining = _allTemplates.value.filter { it.id != id }
            if (remaining.isNotEmpty()) {
                val nextSelected = remaining.firstOrNull { it.isSelected } ?: remaining.first()
                _template.value = nextSelected
                if (!_allTemplates.value.any { it.id != id && it.isSelected }) {
                    templateRepository.setSelectedTemplate(nextSelected.id)
                }
            } else {
                createNewTemplate()
            }
            onDelete()
        }
    }
}
