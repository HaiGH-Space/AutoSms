package com.gmail.nthai.data.repository

import com.gmail.nthai.data.local.dao.SendingLogDao
import com.gmail.nthai.data.local.mapper.toDomain
import com.gmail.nthai.data.local.mapper.toEntity
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.repository.SendingLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SendingLogRepositoryImpl(
    private val sendingLogDao: SendingLogDao
) : SendingLogRepository {

    override fun getAllLogs(): Flow<List<LogRecord>> {
        return sendingLogDao.getAllLogs().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertLog(log: LogRecord): Long {
        return sendingLogDao.insertLog(log.toEntity())
    }

    override suspend fun updateLogStatus(id: Long, status: LogStatus, errorMessage: String?) {
        sendingLogDao.updateLogStatus(id, status.name, errorMessage)
    }

    override suspend fun deleteAllLogs() {
        sendingLogDao.deleteAllLogs()
    }
}
