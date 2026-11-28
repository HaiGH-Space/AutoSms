package com.gmail.nthai.domain.model

data class AutoCallConfig(
    val delaySeconds: Int = 5,
    val selectedCustomerIds: List<Long> = emptyList(),
    val simSlot: Int = 1
)
