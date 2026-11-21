package com.gmail.nthai.data.repository

import com.gmail.nthai.data.excel.ExcelParser
import com.gmail.nthai.data.local.dao.CallRecipientDao
import com.gmail.nthai.data.local.entity.CallRecipientEntity
import com.gmail.nthai.data.local.mapper.toDomain
import com.gmail.nthai.data.local.mapper.toEntity
import com.gmail.nthai.domain.model.CallRecipient
import com.gmail.nthai.domain.model.ImportMode
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.repository.CallRecipientRepository
import com.gmail.nthai.domain.repository.CallRecipientStats
import com.gmail.nthai.util.PhoneNumberUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRecipientRepositoryImpl @Inject constructor(
    private val callRecipientDao: CallRecipientDao,
    private val excelParser: ExcelParser = ExcelParser()
) : CallRecipientRepository {

    override fun getAllRecipients(): Flow<List<CallRecipient>> {
        return callRecipientDao.getAllRecipients().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeStats(): Flow<CallRecipientStats> {
        return callRecipientDao.observeRecipientStats().map { entity ->
            CallRecipientStats(
                totalCount = entity.totalCount,
                pendingCount = entity.pendingCount,
                calledCount = entity.calledCount,
                failedCount = entity.failedCount,
                selectedCount = entity.selectedCount
            )
        }
    }

    override suspend fun getSelectedRecipients(): List<CallRecipient> = withContext(Dispatchers.IO) {
        callRecipientDao.getSelectedRecipientsList().map { it.toDomain() }
    }

    override suspend fun insertRecipient(name: String, phone: String): Long = withContext(Dispatchers.IO) {
        val entity = CallRecipientEntity(
            name = name.trim(),
            phone = phone.trim(),
            status = "PENDING",
            isSelected = true
        )
        callRecipientDao.insertRecipient(entity)
    }

    override suspend fun updateRecipientSelection(id: Long, isSelected: Boolean) = withContext(Dispatchers.IO) {
        callRecipientDao.updateRecipientSelection(id, isSelected)
    }

    override suspend fun updateAllRecipientSelection(isSelected: Boolean) = withContext(Dispatchers.IO) {
        callRecipientDao.updateAllRecipientSelection(isSelected)
    }

    override suspend fun updateRecipientStatus(
        id: Long,
        status: LogStatus,
        durationSeconds: Int,
        note: String
    ) = withContext(Dispatchers.IO) {
        callRecipientDao.updateRecipientStatus(
            id = id,
            status = status.name,
            duration = durationSeconds,
            note = note,
            lastCallTime = System.currentTimeMillis()
        )
    }

    override suspend fun resetAllRecipientStatus() = withContext(Dispatchers.IO) {
        callRecipientDao.resetAllRecipientStatus()
    }

    override suspend fun deleteRecipient(id: Long) = withContext(Dispatchers.IO) {
        callRecipientDao.deleteRecipientById(id)
    }

    override suspend fun deleteAllRecipients() = withContext(Dispatchers.IO) {
        callRecipientDao.deleteAllRecipients()
    }

    override suspend fun importFromExcel(
        inputStream: InputStream,
        phoneColumn: String,
        importMode: ImportMode
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val parseResult = excelParser.parseCustomers(inputStream, phoneColumn)
            val customers = parseResult.getOrThrow()

            if (customers.isEmpty()) {
                return@runCatching 0
            }

            val existingPhones = if (importMode == ImportMode.SKIP_DUPLICATES) {
                callRecipientDao.getAllRecipientPhones().toHashSet()
            } else {
                emptySet()
            }

            val entitiesToInsert = mutableListOf<CallRecipientEntity>()
            for (c in customers) {
                val cleanPhone = PhoneNumberUtils.normalizePhoneNumber(c.phone)
                if (cleanPhone.isBlank()) continue

                if (importMode == ImportMode.SKIP_DUPLICATES && existingPhones.contains(cleanPhone)) {
                    continue
                }

                // Determine name from custom fields if available
                val name = c.customFields["ho_ten"]
                    ?: c.customFields["hoten"]
                    ?: c.customFields["ten"]
                    ?: c.customFields["name"]
                    ?: cleanPhone

                val jsonStr = JSONObject().apply {
                    c.customFields.forEach { (k, v) -> put(k, v) }
                }.toString()

                entitiesToInsert.add(
                    CallRecipientEntity(
                        name = name,
                        phone = cleanPhone,
                        status = "PENDING",
                        isSelected = true,
                        customFieldsJson = jsonStr
                    )
                )
            }

            if (importMode == ImportMode.REPLACE_ALL) {
                callRecipientDao.deleteAllRecipients()
            }

            callRecipientDao.insertRecipients(entitiesToInsert)
            entitiesToInsert.size
        }
    }
}
