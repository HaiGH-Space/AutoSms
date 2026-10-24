package com.gmail.nthai.data.excel

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ExcelParserTest {

    @Test
    fun `imports all spreadsheet columns as custom fields`() {
        val output = ByteArrayOutputStream()
        XSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet("Khách hàng")
            val header = sheet.createRow(0)
            listOf("Họ tên", "Số điện thoại", "Mã hợp đồng", "Địa chỉ", "Gói dịch vụ")
                .forEachIndexed { index, value -> header.createCell(index).setCellValue(value) }

            val customer = sheet.createRow(1)
            listOf("Nguyễn An", "0912345678", "HD001", "Hà Nội", "Premium")
                .forEachIndexed { index, value -> customer.createCell(index).setCellValue(value) }

            workbook.write(output)
        }

        val result = ExcelParser().parseCustomers(ByteArrayInputStream(output.toByteArray()))
        assertTrue(result.isSuccess)
        val imported = result.getOrThrow().single()

        assertEquals("0912345678", imported.phone)
        assertEquals("Nguyễn An", imported.customFields["Họ tên"])
        assertEquals("HD001", imported.customFields["Mã hợp đồng"])
        assertEquals("Hà Nội", imported.customFields["Địa chỉ"])
        assertEquals("Premium", imported.customFields["Gói dịch vụ"])
    }

    @Test
    fun `imports custom columns when phone is the only recognized header`() {
        val output = ByteArrayOutputStream()
        XSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet("Khách hàng")
            val header = sheet.createRow(0)
            listOf("Số điện thoại", "Khu vực")
                .forEachIndexed { index, value -> header.createCell(index).setCellValue(value) }
            val customer = sheet.createRow(1)
            listOf("0912345678", "Miền Bắc")
                .forEachIndexed { index, value -> customer.createCell(index).setCellValue(value) }
            workbook.write(output)
        }

        val imported = ExcelParser()
            .parseCustomers(ByteArrayInputStream(output.toByteArray()))
            .getOrThrow()
            .single()

        assertEquals("Miền Bắc", imported.customFields["Khu vực"])
    }

    @Test
    fun `imports multiple comma separated phone numbers as individual customers`() {
        val output = ByteArrayOutputStream()
        XSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet("Khách hàng")
            val header = sheet.createRow(0)
            listOf("sdt", "Tên")
                .forEachIndexed { index, value -> header.createCell(index).setCellValue(value) }
            val customer = sheet.createRow(1)
            listOf("0912345678, +84987654321", "Nguyễn An")
                .forEachIndexed { index, value -> customer.createCell(index).setCellValue(value) }
            workbook.write(output)
        }

        val imported = ExcelParser()
            .parseCustomers(ByteArrayInputStream(output.toByteArray()))
            .getOrThrow()

        assertEquals(2, imported.size)
        assertEquals("0912345678", imported[0].phone)
        assertEquals("0912345678", imported[0].customFields["sdt"])
        assertEquals("Nguyễn An", imported[0].customFields["Tên"])

        assertEquals("0987654321", imported[1].phone)
        assertEquals("0987654321", imported[1].customFields["sdt"])
        assertEquals("Nguyễn An", imported[1].customFields["Tên"])
    }

    @Test
    fun `imports sdt column with multiple comma separated numbers and varying formats`() {
        val output = ByteArrayOutputStream()
        XSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet("Khách hàng")
            val header = sheet.createRow(0)
            listOf("Tên khách hàng", "sdt", "Số tiền")
                .forEachIndexed { index, value -> header.createCell(index).setCellValue(value) }
            val customer = sheet.createRow(1)
            listOf("Lê Văn B", "0901234567, 090 876 5432, +84911223344", "500000")
                .forEachIndexed { index, value -> customer.createCell(index).setCellValue(value) }
            workbook.write(output)
        }

        val imported = ExcelParser()
            .parseCustomers(ByteArrayInputStream(output.toByteArray()))
            .getOrThrow()

        assertEquals(3, imported.size)
        assertEquals("0901234567", imported[0].phone)
        assertEquals("0908765432", imported[1].phone)
        assertEquals("0911223344", imported[2].phone)
    }

    @Test
    fun `filters out consecutive commas and invalid phone tokens`() {
        val parser = ExcelParser()
        val raw = "0901234567,,, 090 876 5432 , , +84911223344, abc, 123"
        val cleaned = parser.cleanAndFilterPhoneNumbers(raw)

        assertEquals(listOf("0901234567", "0908765432", "0911223344"), cleaned)
    }

    @Test
    fun `imports using user configured custom phone column name`() {
        val output = ByteArrayOutputStream()
        XSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet("Khách hàng")
            val header = sheet.createRow(0)
            listOf("Họ và Tên", "SĐT_KH", "Ghi chú")
                .forEachIndexed { index, value -> header.createCell(index).setCellValue(value) }
            val customer = sheet.createRow(1)
            listOf("Trần Văn C", "0933445566, 0977889900", "Khách VIP")
                .forEachIndexed { index, value -> customer.createCell(index).setCellValue(value) }
            workbook.write(output)
        }

        val imported = ExcelParser()
            .parseCustomers(ByteArrayInputStream(output.toByteArray()), phoneColumnName = "SĐT_KH")
            .getOrThrow()

        assertEquals(2, imported.size)
        assertEquals("0933445566", imported[0].phone)
        assertEquals("0977889900", imported[1].phone)
        assertEquals("Trần Văn C", imported[0].customFields["Họ và Tên"])
        assertEquals("Khách VIP", imported[0].customFields["Ghi chú"])
    }
}
