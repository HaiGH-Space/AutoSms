package com.gmail.nthai.domain.usecase

import com.gmail.nthai.data.excel.ExcelParser
import com.gmail.nthai.domain.model.Customer
import java.io.InputStream

class ImportCustomerExcelUseCase(
    private val excelParser: ExcelParser = ExcelParser()
) {
    operator fun invoke(inputStream: InputStream): Result<List<Customer>> {
        return excelParser.parseCustomers(inputStream)
    }
}
