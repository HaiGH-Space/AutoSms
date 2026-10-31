package com.gmail.nthai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sim_configs")
data class SimConfigEntity(
    @PrimaryKey val simSlot: Int,
    val subscriptionId: Int,
    val dailyLimit: Int,
    val sentCountToday: Int,
    val lastResetDate: String,
    val carrierName: String = ""
)
