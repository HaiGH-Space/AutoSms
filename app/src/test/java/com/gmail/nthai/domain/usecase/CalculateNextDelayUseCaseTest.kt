package com.gmail.nthai.domain.usecase

import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateNextDelayUseCaseTest {

    private val useCase = CalculateNextDelayUseCase()

    @Test
    fun `calculate next delay returns value within range in milliseconds`() {
        val minSec = 10
        val maxSec = 30
        for (i in 1..100) {
            val delayMs = useCase(minSec, maxSec)
            assertTrue("Delay $delayMs ms should be >= 10000", delayMs >= 10000L)
            assertTrue("Delay $delayMs ms should be <= 30000", delayMs <= 30000L)
        }
    }
}
