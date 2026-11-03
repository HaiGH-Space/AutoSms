package com.gmail.nthai.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class SimConfig(
    val subscriptionId: Int,
    val simSlot: Int, // 1 for SIM 1, 2 for SIM 2
    val carrierName: String = "",
    val dailyLimit: Int = getDefaultDailyLimit(carrierName),
    val sentCountToday: Int = 0,
    val lastResetDate: String = ""
) {
    companion object {
        const val DEFAULT_VIETTEL_LIMIT = 40
        const val DEFAULT_VINAPHONE_LIMIT = 50
        const val DEFAULT_MOBIFONE_LIMIT = 50
        const val DEFAULT_VIETNAMOBILE_LIMIT = 100
        const val DEFAULT_GMOBILE_LIMIT = 50
        const val DEFAULT_WINTEL_LIMIT = 50
        const val DEFAULT_ITEL_LIMIT = 50
        const val DEFAULT_UNKNOWN_LIMIT = 40

        fun getDefaultDailyLimit(carrierName: String): Int {
            val normalized = carrierName.trim()
            return when {
                normalized.contains("vietnamobile", ignoreCase = true) -> DEFAULT_VIETNAMOBILE_LIMIT
                normalized.contains("viettel", ignoreCase = true) -> DEFAULT_VIETTEL_LIMIT
                normalized.contains("vinaphone", ignoreCase = true) ||
                    normalized.contains("vina", ignoreCase = true) -> DEFAULT_VINAPHONE_LIMIT
                normalized.contains("mobifone", ignoreCase = true) ||
                    normalized.contains("mobi", ignoreCase = true) -> DEFAULT_MOBIFONE_LIMIT
                normalized.contains("gmobile", ignoreCase = true) ||
                    normalized.contains("beeline", ignoreCase = true) -> DEFAULT_GMOBILE_LIMIT
                normalized.contains("wintel", ignoreCase = true) ||
                    normalized.contains("reddi", ignoreCase = true) -> DEFAULT_WINTEL_LIMIT
                normalized.contains("itel", ignoreCase = true) -> DEFAULT_ITEL_LIMIT
                else -> DEFAULT_UNKNOWN_LIMIT
            }
        }
    }
}
