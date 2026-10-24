package com.gmail.nthai.domain.usecase

import com.gmail.nthai.data.excel.ExcelExporter
import com.gmail.nthai.domain.model.LogRecord
import java.io.OutputStream

class ExportReportExcelUseCase(
    private val excelExporter: ExcelExporter = ExcelExporter()
) {
    operator fun invoke(logs: List<LogRecord>, outputStream: OutputStream): Result<Unit> {
        return excelExporter.exportLogs(logs, outputStream)
    }
}
