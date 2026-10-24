package com.gmail.nthai.data.excel

import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExcelExporter {

    fun exportLogs(logs: List<LogRecord>, outputStream: OutputStream): Result<Unit> {
        return runCatching {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Báo cáo gửi SMS")

            val headerRow = sheet.createRow(0)
            val headers = arrayOf("STT", "Tên KH", "SĐT", "SIM", "Nội dung tin nhắn", "Thời gian", "Trạng thái", "Ghi chú lỗi")

            for (i in headers.indices) {
                val cell = headerRow.createCell(i)
                cell.setCellValue(headers[i])
            }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

            for (i in logs.indices) {
                val log = logs[i]
                val row = sheet.createRow(i + 1)

                row.createCell(0).setCellValue((i + 1).toDouble())
                row.createCell(1).setCellValue(log.customerName)
                row.createCell(2).setCellValue(log.phone)
                row.createCell(3).setCellValue("SIM ${log.simSlot}")
                row.createCell(4).setCellValue(log.messageContent)
                row.createCell(5).setCellValue(dateFormat.format(Date(log.timestamp)))

                val statusText = when (log.status) {
                    LogStatus.SENT_SUCCESS -> "Thành công"
                    LogStatus.FAILED -> "Thất bại"
                    LogStatus.PENDING -> "Chờ gửi"
                }
                row.createCell(6).setCellValue(statusText)
                row.createCell(7).setCellValue(log.errorMessage ?: "")
            }

            for (i in headers.indices) {
                sheet.setColumnWidth(i, 15 * 256)
            }

            workbook.write(outputStream)
            workbook.close()
        }
    }
}
