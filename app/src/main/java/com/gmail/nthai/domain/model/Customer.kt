package com.gmail.nthai.domain.model

import androidx.compose.runtime.Immutable

enum class CustomerStatus {
    PENDING,
    SENT,
    FAILED
}

@Immutable
data class Customer(
    val id: Long = 0,
    val phone: String,
    val customFields: Map<String, String> = emptyMap(),
    val status: CustomerStatus = CustomerStatus.PENDING,
    val isSelected: Boolean = false,
    val sendSimSlot: Int? = null
)
