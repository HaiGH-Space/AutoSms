package com.gmail.nthai.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class SendingRule(
    val minDelaySec: Int = 40,
    val maxDelaySec: Int = 50,
    val startHour: Int = 8,
    val startMinute: Int = 0,
    val endHour: Int = 17,
    val endMinute: Int = 0,
    val phoneColumnName: String = "sdt"
)
