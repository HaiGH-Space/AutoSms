package com.gmail.nthai.domain.model

enum class LogStatus {
    PENDING,
    SENT_SUCCESS,
    FAILED
}

data class LogRecord(
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val phone: String,
    val messageContent: String,
    val simSlot: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: LogStatus = LogStatus.PENDING,
    val errorMessage: String? = null
)
