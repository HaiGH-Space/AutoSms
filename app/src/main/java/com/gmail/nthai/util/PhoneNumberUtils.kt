package com.gmail.nthai.util

object PhoneNumberUtils {

    private val NON_DIGIT_PLUS_REGEX = Regex("""[^\d+]""")

    /**
     * Normalizes a Vietnamese phone number to a standard numeric format (starting with '0', e.g., "0912345678").
     *
     * Examples:
     * - "+84912345678" -> "0912345678"
     * - "84912345678" -> "0912345678"
     * - "091 234 5678" -> "0912345678"
     * - "091-234-5678" -> "0912345678"
     */
    fun normalizePhoneNumber(rawPhone: String): String {
        if (rawPhone.isBlank()) return ""
        var cleaned = rawPhone.replace(NON_DIGIT_PLUS_REGEX, "")
        if (cleaned.startsWith("+84")) {
            cleaned = "0" + cleaned.substring(3)
        } else if (cleaned.startsWith("84") && cleaned.length >= 11) {
            cleaned = "0" + cleaned.substring(2)
        }
        return cleaned
    }
}
