package com.gmail.nthai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class SmsTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val isSelected: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
