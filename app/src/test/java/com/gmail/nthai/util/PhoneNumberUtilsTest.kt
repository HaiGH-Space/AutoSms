package com.gmail.nthai.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNumberUtilsTest {

    @Test
    fun `normalizePhoneNumber removes spaces, dashes and normalizes +84 or 84 to 0`() {
        assertEquals("0912345678", PhoneNumberUtils.normalizePhoneNumber("+84912345678"))
        assertEquals("0912345678", PhoneNumberUtils.normalizePhoneNumber("84912345678"))
        assertEquals("0912345678", PhoneNumberUtils.normalizePhoneNumber("091 234 5678"))
        assertEquals("0912345678", PhoneNumberUtils.normalizePhoneNumber("091-234-5678"))
        assertEquals("0912345678", PhoneNumberUtils.normalizePhoneNumber("091.234.5678"))
        assertEquals("0912345678", PhoneNumberUtils.normalizePhoneNumber("0912345678"))
    }

    @Test
    fun `normalizePhoneNumber handles empty or blank inputs`() {
        assertEquals("", PhoneNumberUtils.normalizePhoneNumber(""))
        assertEquals("", PhoneNumberUtils.normalizePhoneNumber("   "))
    }
}
