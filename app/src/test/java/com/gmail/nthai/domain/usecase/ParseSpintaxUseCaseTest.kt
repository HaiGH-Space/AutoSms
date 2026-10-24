package com.gmail.nthai.domain.usecase

import com.gmail.nthai.domain.model.Customer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ParseSpintaxUseCaseTest {

    private val useCase = ParseSpintaxUseCase()

    private val sampleCustomer = Customer(
        id = 1,
        phone = "0912345678",
        customFields = mapOf(
            "Tên" to "Nguyen Van A",
            "Mã HD" to "HD12345",
            "Số tiền" to "1500000",
            "Hạn" to "15/10/2025"
        )
    )

    @Test
    fun `parse spintax replaces placeholders correctly`() {
        val template = "Kính gửi {Ten_KH}, HĐ {Ma_HD} số tiền {So_Tien} đ hạn {Ngay_Han} SĐT {SDT}."
        val result = useCase(template, sampleCustomer)

        assertTrue(result.contains("Nguyen Van A"))
        assertTrue(result.contains("HD12345"))
        assertTrue(result.contains("1.500.000"))
        assertTrue(result.contains("15/10/2025"))
        assertTrue(result.contains("0912345678"))
    }

    @Test
    fun `parse spintax selects options deterministically with seed`() {
        val template = "{Kính chào|Xin chào|Chào} {Ten_KH}, {vui lòng|xin vui lòng} thanh toán."
        val random1 = Random(42)
        val random2 = Random(42)

        val result1 = useCase(template, sampleCustomer, random1)
        val result2 = useCase(template, sampleCustomer, random2)

        assertEquals(result1, result2)
    }

    @Test
    fun `parse spintax picks option from spintax block`() {
        val template = "{Chao|Kinh chao} {Ten_KH}"
        val result = useCase(template, sampleCustomer)

        val containsOption = result.startsWith("Chao Nguyen Van A") || result.startsWith("Kinh chao Nguyen Van A")
        assertTrue(containsOption)
    }

    @Test
    fun `parse custom Excel column variables without consuming spintax`() {
        val customer = sampleCustomer.copy(
            customFields = mapOf("Tên khách hàng" to "Nguyễn An", "Số tiền" to "250000")
        )

        val result = useCase(
            "Kính gửi {{Tên khách hàng}}, số tiền {{Số tiền}}. {Vui lòng|Xin hãy} thanh toán.",
            customer
        )

        assertTrue(result.startsWith("Kính gửi Nguyễn An, số tiền 250000. "))
        assertTrue(result.endsWith(" thanh toán."))
    }

    @Test
    fun `unknown custom Excel column variable remains in message`() {
        val result = useCase("Xin chào {{Cột không tồn tại}}", sampleCustomer)

        assertEquals("Xin chào {{Cột không tồn tại}}", result)
    }
}
