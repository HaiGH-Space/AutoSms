package com.gmail.nthai.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_logs",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["timestamp"]),
        Index(value = ["status"])
    ]
)
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val phone: String,
    val callDurationSeconds: Int = 0,
    val timestamp: Long,
    val status: String, // e.g. "SUCCESS", "FAILED", "SKIPPED", "CANCELLED"
    val errorMessage: String? = null
)
