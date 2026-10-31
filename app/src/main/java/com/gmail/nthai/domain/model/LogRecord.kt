package com.gmail.nthai.domain.model

import androidx.compose.runtime.Immutable

enum class LogStatus {
    PENDING,
    SENT_SUCCESS,
    FAILED
}

@Immutable
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
