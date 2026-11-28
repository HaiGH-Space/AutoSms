package com.gmail.nthai.data.repository

import com.gmail.nthai.data.local.dao.CallRecipientDao
import com.gmail.nthai.data.local.dao.CallRecipientStatsEntity
import com.gmail.nthai.data.local.entity.CallRecipientEntity
import com.gmail.nthai.domain.model.ImportMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class CallRecipientRepositoryImplTest {

    private class FakeCallRecipientDao : CallRecipientDao {
        val inserted = mutableListOf<CallRecipientEntity>()

        override fun observeRecipientStats(): Flow<CallRecipientStatsEntity> = flowOf(CallRecipientStatsEntity(0, 0, 0, 0, 0))
        override fun getAllRecipients(): Flow<List<CallRecipientEntity>> = flowOf(inserted)
        override suspend fun getAllRecipientPhones(): List<String> = inserted.map { it.phone }
        override suspend fun getSelectedRecipientsList(): List<CallRecipientEntity> = inserted.filter { it.isSelected }
        override suspend fun getRecipientById(id: Long): CallRecipientEntity? = inserted.find { it.id == id }
        override suspend fun insertRecipient(recipient: CallRecipientEntity): Long {
            inserted.add(recipient)
            return inserted.size.toLong()
        }
        override suspend fun insertRecipients(recipients: List<CallRecipientEntity>) {
            inserted.addAll(recipients)
        }
        override suspend fun updateRecipient(recipient: CallRecipientEntity) {}
        override suspend fun updateRecipientSelection(id: Long, isSelected: Boolean) {}
        override suspend fun updateAllRecipientSelection(isSelected: Boolean) {}
        override suspend fun updateRecipientStatus(id: Long, status: String, duration: Int, note: String, lastCallTime: Long) {}
        override suspend fun resetAllRecipientStatus() {}
        override suspend fun deleteRecipient(recipient: CallRecipientEntity) {}
        override suspend fun deleteRecipientById(id: Long) {}
        override suspend fun deleteAllRecipients() { inserted.clear() }
    }

    @Test
    fun `importFromExcel extracts name from specified nameColumn`() = runBlocking {
        val fakeDao = FakeCallRecipientDao()
        val repository = CallRecipientRepositoryImpl(fakeDao)

        val output = ByteArrayOutputStream()
        XSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet("Sheet1")
            val header = sheet.createRow(0)
            listOf("Tên Khách Hàng", "SĐT").forEachIndexed { i, h -> header.createCell(i).setCellValue(h) }
            val row = sheet.createRow(1)
            row.createCell(0).setCellValue("Trần Văn C")
            row.createCell(1).setCellValue("0909123456")
            workbook.write(output)
        }

        val result = repository.importFromExcel(
            inputStream = ByteArrayInputStream(output.toByteArray()),
            phoneColumn = "SĐT",
            nameColumn = "Tên Khách Hàng",
            importMode = ImportMode.REPLACE_ALL
        )

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow())
        assertEquals("Trần Văn C", fakeDao.inserted.single().name)
        assertEquals("0909123456", fakeDao.inserted.single().phone)
    }
}
