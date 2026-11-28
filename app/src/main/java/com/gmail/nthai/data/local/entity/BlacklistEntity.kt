package com.gmail.nthai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "blacklist",
    indices = [
        Index(value = ["phone"], unique = true)
    ]
)
data class BlacklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phone: String,
    val rawPhone: String,
    @ColumnInfo(defaultValue = "''")
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
