package com.gmail.nthai.data.repository

import com.gmail.nthai.data.local.dao.ZaloRecipientDao
import com.gmail.nthai.data.local.dao.ZaloRecipientStatsEntity
import com.gmail.nthai.data.local.dao.ZaloSendingLogDao
import com.gmail.nthai.data.local.mapper.toDomain
import com.gmail.nthai.data.local.mapper.toEntity
import com.gmail.nthai.data.local.mapper.toZaloEntity
import com.gmail.nthai.domain.model.ImportMode
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.ZaloRecipient
import com.gmail.nthai.domain.repository.ZaloRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZaloRepositoryImpl @Inject constructor(
    private val zaloRecipientDao: ZaloRecipientDao,
    private val zaloSendingLogDao: ZaloSendingLogDao
) : ZaloRepository {

    override fun observeStats(): Flow<ZaloRecipientStatsEntity> {
        return zaloRecipientDao.observeRecipientStats()
    }

    override fun observeRecipients(): Flow<List<ZaloRecipient>> {
        return zaloRecipientDao.getAllRecipients().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeLogs(): Flow<List<LogRecord>> {
        return zaloSendingLogDao.getAllLogs().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getPendingRecipients(): List<ZaloRecipient> {
        return zaloRecipientDao.getPendingRecipientsList().map { it.toDomain() }
    }

    override suspend fun insertRecipients(recipients: List<ZaloRecipient>) {
        zaloRecipientDao.insertRecipients(recipients.map { it.toEntity() })
    }

    override suspend fun importRecipients(recipients: List<ZaloRecipient>, mode: ImportMode): Int {
        return when (mode) {
            ImportMode.REPLACE_ALL -> {
                zaloRecipientDao.deleteAllRecipients()
                zaloRecipientDao.insertRecipients(recipients.map { it.toEntity() })
                recipients.size
            }

            ImportMode.APPEND -> {
                zaloRecipientDao.insertRecipients(recipients.map { it.toEntity() })
                recipients.size
            }

            ImportMode.SKIP_DUPLICATES -> {
                val existingPhones = HashSet<String>()
                zaloRecipientDao.getAllRecipientPhones().forEach { raw ->
                    raw.split(',').map(String::trim).filter(String::isNotEmpty)
                        .forEach { existingPhones.add(it) }
                }
                val filtered = mutableListOf<ZaloRecipient>()
                for (recipient in recipients) {
                    val recipientPhones = recipient.phone.split(',').map(String::trim).filter(String::isNotEmpty)
                    if (recipientPhones.isNotEmpty() && recipientPhones.none { existingPhones.contains(it) }) {
                        filtered.add(recipient)
                        existingPhones.addAll(recipientPhones)
                    }
                }
                if (filtered.isNotEmpty()) {
                    zaloRecipientDao.insertRecipients(filtered.map { it.toEntity() })
                }
                filtered.size
            }
        }
    }

    override suspend fun insertRecipient(recipient: ZaloRecipient): Long {
        return zaloRecipientDao.insertRecipient(recipient.toEntity())
    }

    override suspend fun updateRecipientStatus(id: Long, status: String, note: String) {
        zaloRecipientDao.updateRecipientStatus(id, status, note)
    }

    override suspend fun deleteRecipient(id: Long) {
        zaloRecipientDao.deleteRecipientById(id)
    }

    override suspend fun deleteAllRecipients() {
        zaloRecipientDao.deleteAllRecipients()
    }

    override suspend fun resetAllRecipientStatus() {
        zaloRecipientDao.resetAllRecipientStatus()
    }

    override suspend fun resetRecipientStatus(id: Long) {
        zaloRecipientDao.resetRecipientStatusById(id)
    }

    override suspend fun addLog(log: LogRecord): Long {
        return zaloSendingLogDao.insertLog(log.toZaloEntity())
    }

    override suspend fun clearLogs() {
        zaloSendingLogDao.deleteAllLogs()
    }
}
