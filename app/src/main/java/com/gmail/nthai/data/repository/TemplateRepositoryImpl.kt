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

    override fun getTemplate(): Flow<SmsTemplate?> {
        return smsTemplateDao.getTemplate().map { it?.toDomain() }
    }

    override suspend fun saveTemplate(template: SmsTemplate) {
        smsTemplateDao.saveTemplate(template.toEntity())
    }
}
