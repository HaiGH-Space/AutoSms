package com.gmail.nthai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val contractId: String,
    val amount: Double,
    val dueDate: String,
    val phone: String,
    val status: String // PENDING, SENT, FAILED
)
