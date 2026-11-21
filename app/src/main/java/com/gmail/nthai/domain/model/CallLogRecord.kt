package com.gmail.nthai.domain.model

data class CallLogRecord(
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val phone: String,
    val callDurationSeconds: Int = 0,
    val timestamp: Long,
    val status: String,
    val errorMessage: String? = null
)
