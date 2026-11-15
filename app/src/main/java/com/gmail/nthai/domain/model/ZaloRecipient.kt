package com.gmail.nthai.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class ZaloRecipient(
    val id: Long = 0,
    val name: String,
    val phone: String,
    val isZaloFriend: Boolean = false,
    val status: LogStatus = LogStatus.PENDING,
    val note: String = "",
    val lastSentTime: Long = 0,
    val customFieldsJson: String = "{}"
)
