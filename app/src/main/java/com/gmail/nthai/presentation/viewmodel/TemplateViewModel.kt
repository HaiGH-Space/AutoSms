package com.gmail.nthai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.SmsTemplate
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
    private val parseSpintaxUseCase: ParseSpintaxUseCase
) : ViewModel() {

    private val _template = MutableStateFlow(SmsTemplate())
    val template: StateFlow<SmsTemplate> = _template.asStateFlow()

    private val _preview = MutableStateFlow("")
    val preview: StateFlow<String> = _preview.asStateFlow()

    private val sampleCustomer = Customer(
        name = "Nguyễn Văn A",
        contractId = "HD001",
        amount = 1500000.0,
        dueDate = "20/10/2025",
        phone = "0987654321"
    )

    init {
        viewModelScope.launch {
            templateRepository.getTemplate().collectLatest { savedTemplate ->
                if (savedTemplate != null) {
                    _template.value = savedTemplate
                    generateRandomPreview()
                }
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

    fun insertTag(tag: String) {
        val currentContent = _template.value.content
        _template.value = _template.value.copy(content = "$currentContent $tag".trim())
        generateRandomPreview()
    }

    fun generateRandomPreview() {
        _preview.value = parseSpintaxUseCase(_template.value.content, sampleCustomer)
    }

    fun saveTemplate(onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            templateRepository.saveTemplate(_template.value)
            onSaved()
        }
    }
}
