package com.gmail.nthai.domain.model

enum class CustomerStatus {
    PENDING,
    SENT,
    FAILED
}

data class Customer(
    val id: Long = 0,
    val name: String,
    val contractId: String,
    val amount: Double,
    val dueDate: String,
    val phone: String,
    val status: CustomerStatus = CustomerStatus.PENDING
)
