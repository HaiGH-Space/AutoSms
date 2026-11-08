package com.gmail.nthai.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private const val DEFAULT_DATE_FORMAT = "dd/MM/yyyy"
    private const val DEFAULT_DATETIME_FORMAT = "HH:mm · dd/MM/yyyy"
    private const val SHORT_DATETIME_FORMAT = "HH:mm:ss dd/MM"

    fun formatPickerDate(
        timestamp: Long,
        pattern: String = DEFAULT_DATE_FORMAT
    ): String {
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        return LocalDate.from(Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC)).format(formatter)
    }

    fun formatDateTime(
        timestamp: Long,
        pattern: String = DEFAULT_DATETIME_FORMAT
    ): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatShortDateTime(timestamp: Long): String {
        return formatDateTime(timestamp, SHORT_DATETIME_FORMAT)
    }

    fun localStartOfDay(pickerTimestamp: Long): Long {
        val selectedDate = LocalDate.from(Instant.ofEpochMilli(pickerTimestamp).atZone(ZoneOffset.UTC))
        return selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun localStartOfNextDay(pickerTimestamp: Long): Long {
        val selectedDate = LocalDate.from(Instant.ofEpochMilli(pickerTimestamp).atZone(ZoneOffset.UTC))
        return selectedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
