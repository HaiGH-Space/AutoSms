package com.gmail.nthai.data.local.mapper

import com.gmail.nthai.data.local.entity.ZaloRecipientEntity
import com.gmail.nthai.data.local.entity.ZaloSendingLogEntity
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.model.ZaloRecipient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ZaloMapperTest {

    @Test
    fun recipientEntityToDomain_mapsAllFieldsCorrectly() {
        val entity = ZaloRecipientEntity(
            id = 42L,
            name = "Nguyen Van A",
            phone = "0912345678",
            isZaloFriend = true,
            status = "SENT_SUCCESS",
            note = "Đã gửi qua Trợ năng Zalo",
            lastSentTime = 1700000000L,
            customFieldsJson = "{\"key\":\"value\"}"
        )

        val domain = entity.toDomain()

        assertEquals(42L, domain.id)
        assertEquals("Nguyen Van A", domain.name)
        assertEquals("0912345678", domain.phone)
        assertEquals(true, domain.isZaloFriend)
        assertEquals(LogStatus.SENT_SUCCESS, domain.status)
        assertEquals("Đã gửi qua Trợ năng Zalo", domain.note)
        assertEquals(1700000000L, domain.lastSentTime)
        assertEquals("{\"key\":\"value\"}", domain.customFieldsJson)
    }

    @Test
    fun recipientEntityToDomain_invalidStatus_fallsBackToPending() {
        val entity = ZaloRecipientEntity(
            id = 1L,
            name = "Test",
            phone = "0987654321",
            status = "UNKNOWN_OR_INVALID"
        )

        val domain = entity.toDomain()

        assertEquals(LogStatus.PENDING, domain.status)
    }

    @Test
    fun recipientDomainToEntity_mapsAllFieldsCorrectly() {
        val domain = ZaloRecipient(
            id = 100L,
            name = "Tran Thi B",
            phone = "0987654321",
            isZaloFriend = false,
            status = LogStatus.FAILED,
            note = "Số không tồn tại",
            lastSentTime = 1700001234L,
            customFieldsJson = "{}"
        )

        val entity = domain.toEntity()

        assertEquals(100L, entity.id)
        assertEquals("Tran Thi B", entity.name)
        assertEquals("0987654321", entity.phone)
        assertEquals(false, entity.isZaloFriend)
        assertEquals("FAILED", entity.status)
        assertEquals("Số không tồn tại", entity.note)
        assertEquals(1700001234L, entity.lastSentTime)
        assertEquals("{}", entity.customFieldsJson)
    }

    @Test
    fun zaloSendingLogEntityToDomain_mapsAllFieldsCorrectly() {
        val entity = ZaloSendingLogEntity(
            id = 55L,
            recipientId = 12L,
            recipientName = "Le Van C",
            phone = "0901234567",
            messageContent = "Xin chao!",
            timestamp = 1710000000L,
            status = "SENT_SUCCESS",
            errorMessage = null
        )

        val domain = entity.toDomain()

        assertEquals(55L, domain.id)
        assertEquals(12L, domain.customerId)
        assertEquals("Le Van C", domain.customerName)
        assertEquals("0901234567", domain.phone)
        assertEquals("Xin chao!", domain.messageContent)
        assertEquals(0, domain.simSlot)
        assertEquals(1710000000L, domain.timestamp)
        assertEquals(LogStatus.SENT_SUCCESS, domain.status)
        assertNull(domain.errorMessage)
    }

    @Test
    fun logRecordToZaloEntity_mapsAllFieldsCorrectly() {
        val domain = LogRecord(
            id = 88L,
            customerId = 23L,
            customerName = "Hoang D",
            phone = "0933333333",
            messageContent = "Chuc mung!",
            simSlot = 0,
            timestamp = 1715000000L,
            status = LogStatus.FAILED,
            errorMessage = "Timeout"
        )

        val entity = domain.toZaloEntity()

        assertEquals(88L, entity.id)
        assertEquals(23L, entity.recipientId)
        assertEquals("Hoang D", entity.recipientName)
        assertEquals("0933333333", entity.phone)
        assertEquals("Chuc mung!", entity.messageContent)
        assertEquals(1715000000L, entity.timestamp)
        assertEquals("FAILED", entity.status)
        assertEquals("Timeout", entity.errorMessage)
    }
}
