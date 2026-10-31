package com.gmail.nthai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["status"]),
        Index(value = ["isSelected"]),
        Index(value = ["sendSimSlot"]),
        Index(value = ["isSelected", "sendSimSlot"])
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phone: String,
    @ColumnInfo(defaultValue = "'{}'")
    val customFieldsJson: String = "{}",
    val status: String, // PENDING, SENT, FAILED
    @ColumnInfo(defaultValue = "1")
    val isSelected: Boolean = true,
    @ColumnInfo(defaultValue = "1")
    val sendSimSlot: Int? = 1
)
