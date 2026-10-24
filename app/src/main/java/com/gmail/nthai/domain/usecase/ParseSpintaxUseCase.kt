package com.gmail.nthai.domain.usecase

import com.gmail.nthai.domain.model.Customer
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.random.Random

class ParseSpintaxUseCase {

    private val spintaxRegex = Regex("\\{([^{}]+)\\}")

    operator fun invoke(template: String, customer: Customer, random: Random = Random.Default): String {
        var result = template

        val formattedAmount = try {
            val symbols = DecimalFormatSymbols(Locale("vi", "VN")).apply {
                groupingSeparator = '.'
            }
            DecimalFormat("#,###", symbols).format(customer.amount)
        } catch (_: Exception) {
            customer.amount.toLong().toString()
        }

        result = result.replace("{Ten_KH}", customer.name)
            .replace("{So_Tien}", formattedAmount)
            .replace("{Ma_HD}", customer.contractId)
            .replace("{Ngay_Han}", customer.dueDate)
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
