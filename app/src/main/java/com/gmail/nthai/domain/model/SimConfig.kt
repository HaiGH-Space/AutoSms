package com.gmail.nthai.domain.model

data class SimConfig(
    val subscriptionId: Int,
    val simSlot: Int, // 1 for SIM 1, 2 for SIM 2
    val dailyLimit: Int = 100,
    val sentCountToday: Int = 0,
    val lastResetDate: String = ""
)
