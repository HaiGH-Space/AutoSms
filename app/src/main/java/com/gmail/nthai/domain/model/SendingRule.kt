package com.gmail.nthai.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class SendingRule(
    val minDelaySec: Int = 50,
    val maxDelaySec: Int = 50,
    val startHour: Int = 8,
    val endHour: Int = 17,
    val phoneColumnName: String = "sdt"
)
