package com.gmail.nthai.domain.usecase

import com.gmail.nthai.domain.model.SendingRule
import com.gmail.nthai.domain.model.SimConfig

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class OutsideTimeFrame(
        val currentHour: Int,
        val startHour: Int,
        val endHour: Int,
        val currentMinute: Int = 0,
        val startMinute: Int = 0,
        val endMinute: Int = 0
    ) : ValidationResult()
    data class ExceededLimit(val simSlot: Int, val dailyLimit: Int, val sentToday: Int) : ValidationResult()
}

class ValidateSendingConstraintUseCase {

    operator fun invoke(
        rule: SendingRule,
        currentHour: Int,
        simConfig: SimConfig,
        currentMinute: Int = 0
    ): ValidationResult {
        val currentTime = currentHour * 60 + currentMinute
        val startTime = rule.startHour * 60 + rule.startMinute
        val endTime = rule.endHour * 60 + rule.endMinute
        if (currentTime < startTime || currentTime >= endTime) {
            return ValidationResult.OutsideTimeFrame(
                currentHour = currentHour,
                startHour = rule.startHour,
                endHour = rule.endHour,
                currentMinute = currentMinute,
                startMinute = rule.startMinute,
                endMinute = rule.endMinute
            )
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
