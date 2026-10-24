package com.gmail.nthai.domain.usecase

import com.gmail.nthai.domain.model.SendingRule
import com.gmail.nthai.domain.model.SimConfig
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateSendingConstraintUseCaseTest {

    private val useCase = ValidateSendingConstraintUseCase()

    private val rule = SendingRule(
        minDelaySec = 10,
        maxDelaySec = 30,
        startHour = 8,
        endHour = 17
    )

    private val validSim = SimConfig(
        subscriptionId = 1,
        simSlot = 1,
        dailyLimit = 100,
        sentCountToday = 50
    )

    @Test
    fun `returns Valid when hour is within range and limit not exceeded`() {
        val result = useCase(rule, currentHour = 10, simConfig = validSim)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `returns OutsideTimeFrame when current hour is before start hour`() {
        val result = useCase(rule, currentHour = 7, simConfig = validSim)
        assertTrue(result is ValidationResult.OutsideTimeFrame)
    }

    @Test
    fun `returns OutsideTimeFrame when current hour is after end hour`() {
        val result = useCase(rule, currentHour = 18, simConfig = validSim)
        assertTrue(result is ValidationResult.OutsideTimeFrame)
    }

    @Test
    fun `returns ExceededLimit when sent count reaches daily limit`() {
        val maxedSim = validSim.copy(sentCountToday = 100)
        val result = useCase(rule, currentHour = 10, simConfig = maxedSim)
        assertTrue(result is ValidationResult.ExceededLimit)
    }
}
