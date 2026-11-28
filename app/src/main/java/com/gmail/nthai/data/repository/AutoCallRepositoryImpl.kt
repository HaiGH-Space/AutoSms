package com.gmail.nthai.data.repository

import com.gmail.nthai.data.local.dao.CallLogDao
import com.gmail.nthai.data.local.entity.CallLogEntity
import com.gmail.nthai.domain.model.CallLogRecord
import com.gmail.nthai.domain.repository.AutoCallRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoCallRepositoryImpl @Inject constructor(
    private val callLogDao: CallLogDao
) : AutoCallRepository {

    override fun getAllCallLogs(): Flow<List<CallLogRecord>> {
        return callLogDao.getAllCallLogs().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun insertCallLog(record: CallLogRecord): Long {
        return callLogDao.insertCallLog(record.toEntity())
    }

    override suspend fun updateCallLogStatus(
        id: Long,
        status: String,
        durationSeconds: Int,
        errorMessage: String?
    ) {
        callLogDao.updateCallLogStatus(id, status, durationSeconds, errorMessage)
    }

    override suspend fun deleteAllCallLogs() {
        callLogDao.deleteAllCallLogs()
    }

    private fun CallLogEntity.toDomainModel(): CallLogRecord {
        return CallLogRecord(
            id = id,
            customerId = customerId,
            customerName = customerName,
            phone = phone,
            callDurationSeconds = callDurationSeconds,
            timestamp = timestamp,
            status = status,
            errorMessage = errorMessage
        )
    }

    private fun CallLogRecord.toEntity(): CallLogEntity {
        return CallLogEntity(
            id = id,
            customerId = customerId,
            customerName = customerName,
            phone = phone,
            callDurationSeconds = callDurationSeconds,
            timestamp = timestamp,
            status = status,
            errorMessage = errorMessage
        )
    }
}
