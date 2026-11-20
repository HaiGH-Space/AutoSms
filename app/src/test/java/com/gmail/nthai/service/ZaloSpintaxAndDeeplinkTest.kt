package com.gmail.nthai.service

import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.usecase.ParseSpintaxUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZaloSpintaxAndDeeplinkTest {

    private val parseSpintaxUseCase = ParseSpintaxUseCase()

    @Test
    fun parseMessageForZaloRecipient_replacesCustomerNameCorrectly() {
        val template = "Chào {tên}, chúc bạn ngày tốt lành!"
        val customer = Customer(
            id = 1L,
            phone = "0912345678",
            customFields = mapOf("tên" to "Nguyễn Văn A")
        )

        val result = parseSpintaxUseCase(template, customer)
        assertEquals("Chào Nguyễn Văn A, chúc bạn ngày tốt lành!", result)
    }

    @Test
    fun parseMessageForZaloRecipient_resolvesSpintaxVariations() {
        val template = "{Chào|Xin chào|Hello} bạn!"
        val customer = Customer(
            id = 2L,
            phone = "0987654321",
            customFields = emptyMap()
        )

        val possible = setOf("Chào bạn!", "Xin chào bạn!", "Hello bạn!")
        val result = parseSpintaxUseCase(template, customer)
        assertTrue(possible.contains(result))
    }

    @Test
    fun cleanPhoneForZaloDeeplink_stripsNonNumericExceptPlus() {
        val rawPhones = listOf(
            "0912 345 678" to "0912345678",
            "+84 912-345-678" to "+84912345678",
            "(028) 38.123.456" to "02838123456"
        )

        for ((input, expected) in rawPhones) {
            val cleaned = input.replace(Regex("[^0-9+]"), "")
            assertEquals(expected, cleaned)
            val uriString = "https://zalo.me/$cleaned"
            assertTrue(uriString.startsWith("https://zalo.me/"))
            assertEquals("https://zalo.me/$expected", uriString)
        }
    }
}
