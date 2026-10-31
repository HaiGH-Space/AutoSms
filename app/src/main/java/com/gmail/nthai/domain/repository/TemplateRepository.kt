package com.gmail.nthai.domain.repository

import com.gmail.nthai.domain.model.SmsTemplate
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun getAllTemplates(): Flow<List<SmsTemplate>>
    fun getSelectedTemplate(): Flow<SmsTemplate?>
    fun getTemplateById(id: Long): Flow<SmsTemplate?>
    suspend fun saveTemplate(template: SmsTemplate): Long
    suspend fun deleteTemplate(id: Long)
    suspend fun setSelectedTemplate(id: Long)
}
