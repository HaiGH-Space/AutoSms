package com.gmail.nthai.domain.model

enum class CustomerStatus {
    PENDING,
    SENT,
    FAILED
}

data class Customer(
    val id: Long = 0,
    val phone: String,
    val customFields: Map<String, String> = emptyMap(),
    val status: CustomerStatus = CustomerStatus.PENDING,
    val isSelected: Boolean = true
)
