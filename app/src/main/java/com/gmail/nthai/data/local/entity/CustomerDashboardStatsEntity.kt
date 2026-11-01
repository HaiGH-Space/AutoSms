package com.gmail.nthai.data.local.entity

data class CustomerDashboardStatsEntity(
    val totalCustomers: Int,
    val sim1AssignedCount: Int,
    val sim1ReadyCount: Int,
    val sim2AssignedCount: Int,
    val sim2ReadyCount: Int
)
