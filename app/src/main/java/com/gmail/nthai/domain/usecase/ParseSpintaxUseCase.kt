package com.gmail.nthai.domain.usecase

import com.gmail.nthai.domain.model.Customer
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.random.Random

class ParseSpintaxUseCase {

    operator fun invoke(template: String, customer: Customer, random: Random = Random.Default): String {
        var result = COLUMN_VARIABLE_REGEX.replace(template) { match ->
            val colName = match.groupValues[1].trim()
            customer.customFields.entries.find { (key, _) ->
                key.trim().equals(colName, ignoreCase = true)
            }?.value ?: match.value
        }

        val name = customer.customFields.entries.find { (k, _) ->
            val key = k.lowercase(Locale.ROOT)
            key.contains("tên") || key.contains("ten") || key.contains("khách")
        }?.value ?: "Khách hàng"

        val rawAmount = customer.customFields.entries.find { (k, _) ->
            val key = k.lowercase(Locale.ROOT)
            key.contains("tiền") || key.contains("nợ") || key.contains("amount")
        }?.value ?: "0"

        val formattedAmount = try {
            val amountNum = NON_NUMERIC_REGEX.replace(rawAmount, "").toDoubleOrNull() ?: 0.0
            DECIMAL_FORMAT_THREAD_LOCAL.get()?.format(amountNum) ?: rawAmount
        } catch (_: Exception) {
            rawAmount
        }

        val contractId = customer.customFields.entries.find { (k, _) ->
            val key = k.lowercase(Locale.ROOT)
            key.contains("mã") || key.contains("hợp đồng") || key.contains("hd")
        }?.value ?: ""

        val dueDate = customer.customFields.entries.find { (k, _) ->
            val key = k.lowercase(Locale.ROOT)
            key.contains("hạn") || key.contains("ngày")
        }?.value ?: ""

        result = result.replace("{Ten_KH}", name)
            .replace("{So_Tien}", formattedAmount)
            .replace("{Ma_HD}", contractId)
            .replace("{Ngay_Han}", dueDate)
            .replace("{SDT}", customer.phone)

        while (SPINTAX_REGEX.containsMatchIn(result)) {
            result = SPINTAX_REGEX.replace(result) { matchResult ->
                val options = matchResult.groupValues[1].split("|")
                options[random.nextInt(options.size)]
            }
        }

        return result
    }

    companion object {
        private val SPINTAX_REGEX = Regex("\\{([^{}]*\\|[^{}]*)\\}")
        private val COLUMN_VARIABLE_REGEX = Regex("\\{\\{?([^{}|]+)\\}\\}?")
        private val NON_NUMERIC_REGEX = Regex("[^0-9.]")

        private val DECIMAL_FORMAT_THREAD_LOCAL = ThreadLocal.withInitial {
            val symbols = DecimalFormatSymbols(Locale("vi", "VN")).apply {
                groupingSeparator = '.'
            }
            DecimalFormat("#,###", symbols)
        }
    }
}
