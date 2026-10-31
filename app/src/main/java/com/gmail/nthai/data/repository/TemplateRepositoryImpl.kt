package com.gmail.nthai.data.repository

import com.gmail.nthai.data.local.dao.SmsTemplateDao
import com.gmail.nthai.data.local.mapper.toDomain
import com.gmail.nthai.data.local.mapper.toEntity
import com.gmail.nthai.domain.model.SmsTemplate
import com.gmail.nthai.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TemplateRepositoryImpl(
    private val smsTemplateDao: SmsTemplateDao
) : TemplateRepository {

    override fun getAllTemplates(): Flow<List<SmsTemplate>> {
        return smsTemplateDao.getAllTemplates().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSelectedTemplate(): Flow<SmsTemplate?> {
        return smsTemplateDao.getSelectedTemplate().map { it?.toDomain() }
    }

    override fun getTemplateById(id: Long): Flow<SmsTemplate?> {
        return smsTemplateDao.getTemplateById(id).map { it?.toDomain() }
    }

    override suspend fun saveTemplate(template: SmsTemplate): Long {
        return smsTemplateDao.saveTemplate(template.toEntity())
    }

    override suspend fun deleteTemplate(id: Long) {
        smsTemplateDao.deleteTemplateById(id)
    }

    override suspend fun setSelectedTemplate(id: Long) {
        smsTemplateDao.setSelectedTemplate(id)
    }
}
