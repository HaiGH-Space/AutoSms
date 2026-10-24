package com.gmail.nthai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sending_rules")
data class SendingRuleEntity(
    @PrimaryKey val id: Int = 1,
    val minDelaySec: Int,
    val maxDelaySec: Int,
    val startHour: Int,
    val endHour: Int
)
