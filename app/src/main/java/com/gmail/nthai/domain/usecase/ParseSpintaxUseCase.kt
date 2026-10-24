package com.gmail.nthai.domain.usecase

import com.gmail.nthai.domain.model.Customer
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.random.Random

class ParseSpintaxUseCase {

    private val spintaxRegex = Regex("\\{([^{}]*\\|[^{}]*)\\}")
    private val columnVariableRegex = Regex("\\{\\{([^{}]+)\\}\\}")

    operator fun invoke(template: String, customer: Customer, random: Random = Random.Default): String {
        var result = columnVariableRegex.replace(template) { match ->
            val colName = match.groupValues[1].trim()
            customer.customFields.entries.find { (key, _) ->
                key.trim().equals(colName, ignoreCase = true)
            }?.value ?: match.value
        }

        val name = customer.customFields.entries.find { (k, _) ->
            val key = k.lowercase()
            key.contains("tên") || key.contains("ten") || key.contains("khách")
        }?.value ?: "Khách hàng"

        val rawAmount = customer.customFields.entries.find { (k, _) ->
            val key = k.lowercase()
            key.contains("tiền") || key.contains("nợ") || key.contains("amount")
        }?.value ?: "0"

        val formattedAmount = try {
            val amountNum = rawAmount.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
            val symbols = DecimalFormatSymbols(Locale("vi", "VN")).apply {
                groupingSeparator = '.'
            }
            DecimalFormat("#,###", symbols).format(amountNum)
        } catch (_: Exception) {
            rawAmount
        }

        val contractId = customer.customFields.entries.find { (k, _) ->
            val key = k.lowercase()
            key.contains("mã") || key.contains("hợp đồng") || key.contains("hd")
        }?.value ?: ""

        val dueDate = customer.customFields.entries.find { (k, _) ->
            val key = k.lowercase()
            key.contains("hạn") || key.contains("ngày")
        }?.value ?: ""

        result = result.replace("{Ten_KH}", name)
            .replace("{So_Tien}", formattedAmount)
            .replace("{Ma_HD}", contractId)
            .replace("{Ngay_Han}", dueDate)
            .replace("{SDT}", customer.phone)

        while (spintaxRegex.containsMatchIn(result)) {
            result = spintaxRegex.replace(result) { matchResult ->
                val options = matchResult.groupValues[1].split("|")
                options[random.nextInt(options.size)]
            }
        }

        return result
    }
}
