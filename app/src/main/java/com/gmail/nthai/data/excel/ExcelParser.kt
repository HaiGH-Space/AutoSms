package com.gmail.nthai.data.excel

import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import com.gmail.nthai.util.PoiInitializer
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.apache.poi.xssf.model.StylesTable
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.xml.parsers.SAXParserFactory

class ExcelParser {

    init {
        PoiInitializer.init()
    }

    fun parseCustomers(
        inputStream: InputStream,
        phoneColumnName: String = "sdt"
    ): Result<List<Customer>> {
        return runCatching {
            parseCustomersSax(inputStream, phoneColumnName)
                ?: throw IllegalArgumentException("Không thể đọc dữ liệu từ file Excel .xlsx")
        }
    }

    fun getExcelHeaders(inputStream: InputStream): Result<List<String>> {
        return runCatching {
            getExcelHeadersSax(inputStream)
                ?: throw IllegalArgumentException("Không thể đọc tiêu đề cột từ file Excel .xlsx")
        }
    }

    private fun parseCustomersSax(
        inputStream: InputStream,
        phoneColumnName: String
    ): List<Customer>? {
        val rows = readSheetRowsSax(inputStream, maxRowsToRead = MAX_ROWS_PARSED) ?: return null
        if (rows.isEmpty()) return emptyList()

        val targetPhoneHeader = phoneColumnName.trim().lowercase(Locale.ROOT)
        var headerRowIndex = -1
        var phoneCol = -1
        var headers = emptyList<String>()

        // Find header row (check first 10 rows)
        for (r in 0 until rows.size.coerceAtMost(10)) {
            val rowMap = rows[r]
            val maxCol = (rowMap.keys.maxOrNull() ?: -1)
            if (maxCol < 0) continue

            val rowHeaders = (0..maxCol).map { rowMap[it]?.trim().orEmpty() }
            var candidatePhoneCol = -1

            // 1. Exact match with targetPhoneHeader
            if (targetPhoneHeader.isNotEmpty()) {
                for ((c, header) in rowHeaders.withIndex()) {
                    if (header.lowercase(Locale.ROOT) == targetPhoneHeader) {
                        candidatePhoneCol = c
                        break
                    }
                }
            }

            // 2. Contains match with targetPhoneHeader
            if (candidatePhoneCol == -1 && targetPhoneHeader.isNotEmpty()) {
                for ((c, header) in rowHeaders.withIndex()) {
                    if (header.lowercase(Locale.ROOT).contains(targetPhoneHeader)) {
                        candidatePhoneCol = c
                        break
                    }
                }
            }

            // 3. Fallback to default phone keywords
            if (candidatePhoneCol == -1) {
                for ((c, header) in rowHeaders.withIndex()) {
                    val valStr = header.lowercase(Locale.ROOT)
                    if (valStr == "sdt" || valStr == "sđt" || valStr == "phone") {
                        candidatePhoneCol = c
                        break
                    } else if (candidatePhoneCol == -1 && (
                                valStr.contains("sđt") ||
                                        valStr.contains("sdt") ||
                                        valStr.contains("thoại") ||
                                        valStr.contains("phone")
                                )
                    ) {
                        candidatePhoneCol = c
                    }
                }
            }

            if (candidatePhoneCol != -1) {
                headerRowIndex = r
                headers = rowHeaders
                phoneCol = candidatePhoneCol
                break
            }
        }

        val customers = mutableListOf<Customer>()
        val seenPhones = mutableSetOf<String>()
        val startRowIndex = if (headerRowIndex != -1) headerRowIndex + 1 else 1

        for (r in startRowIndex until rows.size) {
            val rowMap = rows[r]
            val rawPhone = if (phoneCol != -1) rowMap[phoneCol]?.trim().orEmpty() else ""
            val normalizedPhones = cleanAndFilterPhoneNumbers(rawPhone)

            if (normalizedPhones.isEmpty()) continue

            val rowCustomFields = linkedMapOf<String, String>()
            headers.forEachIndexed { index, header ->
                if (header.isNotBlank()) {
                    rowCustomFields[header] = rowMap[index]?.trim().orEmpty()
                }
            }

            for (singlePhone in normalizedPhones) {
                if (!seenPhones.add(singlePhone)) {
                    continue
                }
                val itemCustomFields = rowCustomFields.toMutableMap()
                if (phoneCol in 0 until headers.size) {
                    val phoneHeader = headers[phoneCol]
                    if (phoneHeader.isNotBlank()) {
                        itemCustomFields[phoneHeader] = singlePhone
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

        return customers
    }

    private fun getExcelHeadersSax(inputStream: InputStream): List<String>? {
        val rows = readSheetRowsSax(inputStream, maxRowsToRead = 10) ?: return null
        for (rowMap in rows) {
            val maxCol = rowMap.keys.maxOrNull() ?: -1
            if (maxCol < 0) continue
            val rowHeaders = (0..maxCol)
                .map { rowMap[it]?.trim().orEmpty() }
                .filter { it.isNotBlank() }
            if (rowHeaders.isNotEmpty()) {
                return rowHeaders
            }
        }
        return emptyList()
    }

    private fun readSheetRowsSax(
        inputStream: InputStream,
        maxRowsToRead: Int
    ): List<Map<Int, String>>? {
        val rows = mutableListOf<Map<Int, String>>()
        var pkg: OPCPackage? = null
        try {
            pkg = OPCPackage.open(inputStream)
            val reader = XSSFReader(pkg)
            val sst = ReadOnlySharedStringsTable(pkg)
            val styles = try {
                reader.stylesTable
            } catch (_: Exception) {
                null
            }
            val sheetsData = reader.sheetsData
            if (!sheetsData.hasNext()) return null

            val sheetStream = sheetsData.next()
            sheetStream.use { stream ->
                val saxFactory = SAXParserFactory.newInstance()
                saxFactory.isNamespaceAware = true
                val xmlReader = saxFactory.newSAXParser().xmlReader
                val handler = XlsxSaxSheetHandler(sst, styles) { _, rowMap ->
                    rows.add(rowMap)
                    if (rows.size >= maxRowsToRead) {
                        false
                    } else {
                        true
                    }
                }
                xmlReader.contentHandler = handler
                try {
                    xmlReader.parse(InputSource(stream))
                } catch (_: XlsxStopParsingException) {
                    // Stopped early by design
                }
            }
            return rows
        } catch (_: Exception) {
            return null
        } finally {
            try {
                pkg?.close()
            } catch (_: Exception) {
            }
        }
    }

    fun cleanAndFilterPhoneNumbers(rawPhone: String): List<String> {
        if (rawPhone.isBlank()) return emptyList()

        val tokens = rawPhone.split(DELIMITER_REGEX)
        val result = mutableListOf<String>()
        val seen = mutableSetOf<String>()

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
                if (seen.add(cleaned)) {
                    result.add(cleaned)
                }
            }
        }
        return result
    }

    companion object {
        private const val MAX_ROWS_PARSED = 50_000
        private val DELIMITER_REGEX = Regex("[,;\\n\\r\\t/]+")
        private val NON_PHONE_CHARS_REGEX = Regex("[^0-9+]")
        private val NON_DIGITS_REGEX = Regex("[^0-9]")

        internal val DATE_FORMATTER_THREAD_LOCAL = ThreadLocal.withInitial {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        }
    }
}

private class XlsxStopParsingException : SAXException("Stop parsing")

private class XlsxSaxSheetHandler(
    private val sst: ReadOnlySharedStringsTable,
    private val styles: StylesTable?,
    private val rowCallback: (rowIndex: Int, rowMap: Map<Int, String>) -> Boolean
) : DefaultHandler() {

    private var currentRowIndex = 0
    private var currentCellCol = -1
    private var currentCellType: String? = null
    private var currentCellStyle: String? = null
    private val currentRowCells = mutableMapOf<Int, String>()
    private val cellContents = StringBuilder()
    private var isReadingText = false

    override fun startElement(
        uri: String,
        localName: String,
        qName: String,
        attributes: Attributes
    ) {
        val name = if (localName.isNotEmpty()) localName else qName
        when (name) {
            "row" -> {
                val rAttr = attributes.getValue("r")
                currentRowIndex = if (rAttr != null) (rAttr.toIntOrNull() ?: (currentRowIndex + 1)) - 1 else currentRowIndex
                currentRowCells.clear()
            }
            "c" -> {
                val rRef = attributes.getValue("r")
                currentCellCol = if (rRef != null) convertColumnRefToIndex(rRef) else currentCellCol + 1
                currentCellType = attributes.getValue("t")
                currentCellStyle = attributes.getValue("s")
                cellContents.setLength(0)
            }
            "v", "t" -> {
                isReadingText = true
                cellContents.setLength(0)
            }
        }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        if (isReadingText) {
            cellContents.appendRange(ch, start, start + length)
        }
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        val name = if (localName.isNotEmpty()) localName else qName
        when (name) {
            "v" -> {
                isReadingText = false
                val rawValue = cellContents.toString()
                val value = formatCellValue(rawValue, currentCellType, currentCellStyle)
                if (currentCellCol >= 0) {
                    currentRowCells[currentCellCol] = value
                }
            }
            "t" -> {
                isReadingText = false
                if (currentCellType == "inlineStr" && currentCellCol >= 0) {
                    currentRowCells[currentCellCol] = cellContents.toString().trim()
                }
            }
            "row" -> {
                val shouldContinue = rowCallback(currentRowIndex, currentRowCells.toMap())
                currentRowIndex++
                if (!shouldContinue) {
                    throw XlsxStopParsingException()
                }
            }
        }
    }

    private fun formatCellValue(rawValue: String, type: String?, styleAttr: String?): String {
        return when (type) {
            "s" -> {
                val idx = rawValue.toIntOrNull()
                if (idx != null) {
                    try {
                        sst.getItemAt(idx).string.trim()
                    } catch (_: Exception) {
                        rawValue.trim()
                    }
                } else rawValue.trim()
            }
            "b" -> if (rawValue == "1") "true" else "false"
            "e" -> ""
            "str", "inlineStr" -> rawValue.trim()
            else -> {
                val num = rawValue.toDoubleOrNull()
                if (num != null) {
                    if (styleAttr != null && styles != null) {
                        val styleIdx = styleAttr.toIntOrNull()
                        if (styleIdx != null && styleIdx in 0 until styles.numCellStyles) {
                            val style = styles.getStyleAt(styleIdx)
                            if (style != null && DateUtil.isADateFormat(style.dataFormat.toInt(), style.dataFormatString)) {
                                val date = DateUtil.getJavaDate(num)
                                val formattedDate = ExcelParser.DATE_FORMATTER_THREAD_LOCAL.get()?.format(date)
                                if (!formattedDate.isNullOrEmpty()) {
                                    return formattedDate
                                }
                            }
                        }
                    }
                    if (num % 1.0 == 0.0) {
                        num.toLong().toString()
                    } else {
                        num.toString()
                    }
                } else {
                    rawValue.trim()
                }
            }
        }
    }

    private fun convertColumnRefToIndex(cellRef: String): Int {
        val colStr = cellRef.takeWhile { it.isLetter() }.uppercase(Locale.ROOT)
        if (colStr.isEmpty()) return -1
        var col = 0
        for (ch in colStr) {
            col = col * 26 + (ch - 'A' + 1)
        }
        return col - 1
    }
}
