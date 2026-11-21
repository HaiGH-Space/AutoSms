package com.gmail.nthai.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class CallRecipient(
    val id: Long = 0,
    val name: String,
    val phone: String,
    val status: LogStatus = LogStatus.PENDING,
    val isSelected: Boolean = true,
    val callDurationSeconds: Int = 0,
    val note: String = "",
    val lastCallTime: Long = 0,
    val customFieldsJson: String = "{}"
)
