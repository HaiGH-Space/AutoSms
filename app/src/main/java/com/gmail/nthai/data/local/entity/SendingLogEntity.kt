package com.gmail.nthai.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sending_logs",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["timestamp"]),
        Index(value = ["status"])
    ]
)
data class SendingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val phone: String,
    val messageContent: String,
    val simSlot: Int,
    val timestamp: Long,
    val status: String,
    val errorMessage: String?
)
