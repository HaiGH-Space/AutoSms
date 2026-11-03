package com.gmail.nthai.data.excel

import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale

class ExcelParser {

    fun parseCustomers(
        inputStream: InputStream,
        phoneColumnName: String = "sdt"
    ): Result<List<Customer>> {
        return runCatching {
            val workbook = WorkbookFactory.create(inputStream)
            try {
                val sheet = workbook.getSheetAt(0) ?: throw IllegalArgumentException("File Excel không có dữ liệu sheet 1")

                val customers = mutableListOf<Customer>()
                var headerRow: Row? = null
                var phoneCol = -1
                var headers = emptyList<String>()
                val targetPhoneHeader = phoneColumnName.trim().lowercase(Locale.ROOT)

                // Find header row (check first 10 rows)
                for (r in 0..sheet.lastRowNum.coerceAtMost(10)) {
                    val row = sheet.getRow(r) ?: continue
                    val rowHeaders = (0 until row.lastCellNum.toInt().coerceAtLeast(0))
                        .map { getCellValueAsString(row.getCell(it)).trim() }

                    var candidatePhoneCol = -1
                    for ((c, header) in rowHeaders.withIndex()) {
                        val valStr = header.lowercase(Locale.ROOT)
                        if (valStr == targetPhoneHeader || valStr == "sdt" || valStr == "sđt") {
                            candidatePhoneCol = c
                            break
                        } else if (candidatePhoneCol == -1 && (
                                    valStr.contains(targetPhoneHeader) ||
                                    valStr.contains("sđt") ||
                                    valStr.contains("sdt") ||
                                    valStr.contains("thoại") ||
                                    valStr.contains("phone")
                                )) {
                            candidatePhoneCol = c
                        }
                    }

                    if (candidatePhoneCol != -1) {
                        headerRow = row
                        headers = rowHeaders
                        phoneCol = candidatePhoneCol
                        break
                    }
                }

                val startRowIndex = if (headerRow != null) headerRow.rowNum + 1 else 1

                for (r in startRowIndex..sheet.lastRowNum) {
                    val row = sheet.getRow(r) ?: continue

                    val rawPhone = if (phoneCol != -1) getCellValueAsString(row.getCell(phoneCol)) else ""
                    val normalizedPhones = cleanAndFilterPhoneNumbers(rawPhone)

                    if (normalizedPhones.isEmpty()) continue

                    val rowCustomFields = linkedMapOf<String, String>()
                    headers.forEachIndexed { index, header ->
                        if (header.isNotBlank()) {
                            rowCustomFields[header] = getCellValueAsString(row.getCell(index))
                        }
                    }

                    // Create a separate customer entry for each phone number
                    for (singlePhone in normalizedPhones) {
                        val itemCustomFields = rowCustomFields.toMutableMap()
                        headers.forEach { header ->
                            val h = header.lowercase(Locale.ROOT)
                            if (h == targetPhoneHeader || h == "sdt" || h == "sđt" || h == "phone" || h.contains("thoại")) {
                                itemCustomFields[header] = singlePhone
                            }
                        }

                        customers.add(
                            Customer(
                                phone = singlePhone,
                                customFields = itemCustomFields,
                                status = CustomerStatus.PENDING
                            )
                        )
                    }
                }

                customers
            } finally {
                workbook.close()
            }
        }
    }

    fun getExcelHeaders(inputStream: InputStream): Result<List<String>> {
        return runCatching {
            val workbook = WorkbookFactory.create(inputStream)
            try {
                val sheet = workbook.getSheetAt(0) ?: throw IllegalArgumentException("File Excel không có dữ liệu sheet 1")
                var headers = emptyList<String>()
                for (r in 0..sheet.lastRowNum.coerceAtMost(10)) {
                    val row = sheet.getRow(r) ?: continue
                    val rowHeaders = (0 until row.lastCellNum.toInt().coerceAtLeast(0))
                        .map { getCellValueAsString(row.getCell(it)).trim() }
                        .filter { it.isNotBlank() }
                    if (rowHeaders.isNotEmpty()) {
                        headers = rowHeaders
                        break
                    }
                }
                headers
            } finally {
                workbook.close()
            }
        }
    }

    fun cleanAndFilterPhoneNumbers(rawPhone: String): List<String> {
        if (rawPhone.isBlank()) return emptyList()

        // Split by common delimiters (comma, semicolon, newline, slash, tab)
        val tokens = rawPhone.split(DELIMITER_REGEX)
        val result = mutableListOf<String>()

        for (token in tokens) {
            val trimmed = token.trim()
            if (trimmed.isBlank()) continue

            var cleaned = NON_PHONE_CHARS_REGEX.replace(trimmed, "")
            if (cleaned.isBlank()) continue

            if (cleaned.startsWith("+84")) {
                cleaned = "0" + cleaned.substring(3)
            } else if (cleaned.startsWith("84") && cleaned.length >= 11) {
                cleaned = "0" + cleaned.substring(2)
            }

            val digitsOnly = NON_DIGITS_REGEX.replace(cleaned, "")
            if (digitsOnly.length in 9..11 && (cleaned.startsWith("0") || cleaned.startsWith("+"))) {
                if (!result.contains(cleaned)) {
                    result.add(cleaned)
                }
            }
        }
        return result
    }

    private fun getCellValueAsString(cell: Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    DATE_FORMATTER_THREAD_LOCAL.get()?.format(cell.dateCellValue).orEmpty()
                } else {
                    val num = cell.numericCellValue
                    if (num % 1.0 == 0.0) {
                        num.toLong().toString()
                    } else {
                        num.toString()
                    }
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                try {
                    cell.numericCellValue.toLong().toString()
                } catch (_: Exception) {
                    cell.stringCellValue.trim()
                }
            }
            else -> ""
        }
    }

    companion object {
        private val DELIMITER_REGEX = Regex("[,;\\n\\r\\t/]+")
        private val NON_PHONE_CHARS_REGEX = Regex("[^0-9+]")
        private val NON_DIGITS_REGEX = Regex("[^0-9]")

        private val DATE_FORMATTER_THREAD_LOCAL = ThreadLocal.withInitial {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        }
    }
}
