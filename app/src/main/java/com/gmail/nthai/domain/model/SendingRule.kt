package com.gmail.nthai.domain.model

data class SendingRule(
    val minDelaySec: Int = 10,
    val maxDelaySec: Int = 30,
    val startHour: Int = 8,
    val endHour: Int = 17
)
