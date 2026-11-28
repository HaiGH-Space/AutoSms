package com.gmail.nthai.domain.model

data class BlacklistItem(
    val id: Long = 0,
    val phone: String,
    val rawPhone: String,
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
