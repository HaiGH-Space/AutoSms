package com.gmail.nthai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sending_logs")
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
