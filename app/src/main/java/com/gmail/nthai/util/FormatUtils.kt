package com.gmail.nthai.util

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun formatCustomFieldValue(value: String): String {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) return value
    val bd = trimmed.toBigDecimalOrNull()
    if (bd != null && trimmed.contains("E", ignoreCase = true)) {
        return bd.stripTrailingZeros().toPlainString()
    }
    return value
}

fun formatExcelNumber(bd: BigDecimal, formatString: String): String {
    if (formatString.isBlank() || formatString.equals("General", ignoreCase = true) || formatString == "@") {
        return bd.stripTrailingZeros().toPlainString()
    }

    val section = formatString.split(";")[0]
    val isPercent = section.contains("%")
    val valueToFormat = if (isPercent) bd.multiply(BigDecimal(100)) else bd

    val cleaned = section.replace(Regex("""_[\s\S]|\\.|[$*"\s()]"""), "")
    val isIntegerPattern = cleaned.contains("0") || cleaned.contains("#")
    if (!isIntegerPattern) {
        return bd.stripTrailingZeros().toPlainString()
    }

    val hasGrouping = cleaned.contains(",")
    val dotIndex = cleaned.indexOf('.')

    val fractionDigits = if (dotIndex != -1) {
        val afterDot = cleaned.substring(dotIndex + 1)
        afterDot.count { it == '0' || it == '#' }
    } else {
        0
    }

    val symbols = DecimalFormatSymbols(Locale.US)
    val patternBuilder = StringBuilder()
    if (hasGrouping) {
        patternBuilder.append("#,##0")
    } else {
        patternBuilder.append("0")
    }

    if (fractionDigits > 0) {
        patternBuilder.append(".")
        repeat(fractionDigits) {
            patternBuilder.append("0")
        }
    }

    if (isPercent) {
        patternBuilder.append("%")
    }

    return try {
        val df = DecimalFormat(patternBuilder.toString(), symbols)
        df.format(valueToFormat)
    } catch (_: Exception) {
        bd.stripTrailingZeros().toPlainString()
    }
}
