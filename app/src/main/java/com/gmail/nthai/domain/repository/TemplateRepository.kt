package com.gmail.nthai.domain.repository

import com.gmail.nthai.domain.model.SmsTemplate
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun getTemplate(): Flow<SmsTemplate?>
    suspend fun saveTemplate(template: SmsTemplate)
}
