package com.gmail.nthai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class SmsTemplateEntity(
    @PrimaryKey val id: Long = 1,
    val title: String,
    val content: String
)
