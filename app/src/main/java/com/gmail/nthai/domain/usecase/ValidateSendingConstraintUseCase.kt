package com.gmail.nthai.domain.usecase

import com.gmail.nthai.domain.model.SendingRule
import com.gmail.nthai.domain.model.SimConfig

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class OutsideTimeFrame(val currentHour: Int, val startHour: Int, val endHour: Int) : ValidationResult()
    data class ExceededLimit(val simSlot: Int, val dailyLimit: Int, val sentToday: Int) : ValidationResult()
}

class ValidateSendingConstraintUseCase {

    operator fun invoke(
        rule: SendingRule,
        currentHour: Int,
        simConfig: SimConfig
    ): ValidationResult {
        if (currentHour < rule.startHour || currentHour >= rule.endHour) {
            return ValidationResult.OutsideTimeFrame(currentHour, rule.startHour, rule.endHour)
        }

        if (simConfig.sentCountToday >= simConfig.dailyLimit) {
            return ValidationResult.ExceededLimit(
                simSlot = simConfig.simSlot,
                dailyLimit = simConfig.dailyLimit,
                sentToday = simConfig.sentCountToday
            )
        }

        return ValidationResult.Valid
    }
}
