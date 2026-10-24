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
        name = "Nguyen Van A",
        contractId = "HD12345",
        amount = 1500000.0,
        dueDate = "15/10/2025",
        phone = "0912345678"
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
}
