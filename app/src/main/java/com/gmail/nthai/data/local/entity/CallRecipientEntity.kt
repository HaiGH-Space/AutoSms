package com.gmail.nthai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_recipients",
    indices = [
        Index(value = ["status"]),
        Index(value = ["phone"]),
        Index(value = ["isSelected"])
    ]
)
data class CallRecipientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    @ColumnInfo(defaultValue = "'PENDING'")
    val status: String = "PENDING", // PENDING, CALLING, CALLED, FAILED
    @ColumnInfo(defaultValue = "1")
    val isSelected: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val callDurationSeconds: Int = 0,
    @ColumnInfo(defaultValue = "''")
    val note: String = "",
    @ColumnInfo(defaultValue = "0")
    val lastCallTime: Long = 0,
    @ColumnInfo(defaultValue = "'{}'")
    val customFieldsJson: String = "{}"
)
