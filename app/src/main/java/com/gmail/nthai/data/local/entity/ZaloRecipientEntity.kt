package com.gmail.nthai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "zalo_recipients",
    indices = [
        Index(value = ["status"]),
        Index(value = ["phone"])
    ]
)
data class ZaloRecipientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    @ColumnInfo(defaultValue = "0")
    val isZaloFriend: Boolean = false,
    @ColumnInfo(defaultValue = "'PENDING'")
    val status: String = "PENDING", // PENDING, SENT_SUCCESS, FAILED
    @ColumnInfo(defaultValue = "''")
    val note: String = "",
    @ColumnInfo(defaultValue = "0")
    val lastSentTime: Long = 0,
    @ColumnInfo(defaultValue = "'{}'")
    val customFieldsJson: String = "{}"
)
