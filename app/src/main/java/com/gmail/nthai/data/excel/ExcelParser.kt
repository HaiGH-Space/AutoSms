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

    fun parseCustomers(inputStream: InputStream): Result<List<Customer>> {
        return runCatching {
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0) ?: throw IllegalArgumentException("File Excel không có dữ liệu sheet 1")

            val customers = mutableListOf<Customer>()
            var headerRow: Row? = null
            var nameCol = -1
            var contractCol = -1
            var phoneCol = -1
            var amountCol = -1
            var dueDateCol = -1

            // Find header row (check first 10 rows)
            for (r in 0..sheet.lastRowNum.coerceAtMost(10)) {
                val row = sheet.getRow(r) ?: continue
                for (c in 0 until row.lastCellNum) {
                    val valStr = getCellValueAsString(row.getCell(c)).lowercase(Locale.ROOT).trim()
                    if (valStr.contains("tên") || valStr.contains("ten") || valStr.contains("khách")) nameCol = c
                    else if (valStr.contains("mã") || valStr.contains("hợp đồng") || valStr.contains("hd")) contractCol = c
                    else if (valStr.contains("sđt") || valStr.contains("sdt") || valStr.contains("thoại") || valStr.contains("phone")) phoneCol = c
                    else if (valStr.contains("tiền") || valStr.contains("nợ") || valStr.contains("amount")) amountCol = c
                    else if (valStr.contains("hạn") || valStr.contains("ngày")) dueDateCol = c
                }
                if (phoneCol != -1 && (nameCol != -1 || contractCol != -1)) {
                    headerRow = row
                    break
                }
            }

            val startRowIndex = if (headerRow != null) headerRow.rowNum + 1 else 1

            for (r in startRowIndex..sheet.lastRowNum) {
                val row = sheet.getRow(r) ?: continue

                val rawName = if (nameCol != -1) getCellValueAsString(row.getCell(nameCol)) else "Khách hàng"
                val rawContract = if (contractCol != -1) getCellValueAsString(row.getCell(contractCol)) else ""
                val rawPhone = if (phoneCol != -1) getCellValueAsString(row.getCell(phoneCol)) else ""
                val rawAmountStr = if (amountCol != -1) getCellValueAsString(row.getCell(amountCol)) else "0"
                val rawDueDate = if (dueDateCol != -1) getCellValueAsString(row.getCell(dueDateCol)) else ""

                val normalizedPhone = normalizePhoneNumber(rawPhone)
                if (normalizedPhone.isBlank()) continue

                val amount = rawAmountStr.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0

                customers.add(
                    Customer(
                        name = rawName.ifBlank { "Khách hàng" },
                        contractId = rawContract,
                        amount = amount,
                        dueDate = rawDueDate,
                        phone = normalizedPhone,
                        status = CustomerStatus.PENDING
                    )
                )
            }

            workbook.close()
            customers
        }
    }

    private fun normalizePhoneNumber(phone: String): String {
        var cleaned = phone.replace("[^0-9+]".toRegex(), "")
        if (cleaned.startsWith("+84")) {
            cleaned = "0" + cleaned.substring(3)
        } else if (cleaned.startsWith("84") && cleaned.length >= 11) {
            cleaned = "0" + cleaned.substring(2)
        }
        return cleaned
    }

    private fun getCellValueAsString(cell: Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cell.dateCellValue)
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
}
