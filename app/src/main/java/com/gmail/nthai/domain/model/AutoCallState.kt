package com.gmail.nthai.domain.model

enum class AutoCallStatus {
    IDLE,
    RUNNING,
    PAUSED,
    FINISHED,
    CANCELLED
}

data class AutoCallProgress(
    val status: AutoCallStatus = AutoCallStatus.IDLE,
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val currentCustomerName: String? = null,
    val currentPhone: String? = null,
    val countdownRemainingSeconds: Int = 0,
    val isWaitingDelay: Boolean = false,
    val errorMessage: String? = null
)
