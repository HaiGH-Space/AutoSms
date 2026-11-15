package com.gmail.nthai.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "zalo_sending_logs",
    indices = [
        Index(value = ["recipientId"]),
        Index(value = ["timestamp"]),
        Index(value = ["status"])
    ]
)
data class ZaloSendingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipientId: Long,
    val recipientName: String,
    val phone: String,
    val messageContent: String,
    val timestamp: Long,
    val status: String,
    val errorMessage: String? = null
)
