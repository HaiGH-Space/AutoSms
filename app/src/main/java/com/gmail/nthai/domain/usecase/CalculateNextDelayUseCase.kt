package com.gmail.nthai.domain.usecase

import kotlin.random.Random

class CalculateNextDelayUseCase {

    operator fun invoke(
        minDelaySec: Int,
        maxDelaySec: Int,
        random: Random = Random.Default
    ): Long {
        val minSec = minDelaySec.coerceAtLeast(1)
        val maxSec = maxDelaySec.coerceAtLeast(minSec)
        val delaySec = if (minSec == maxSec) minSec else random.nextInt(minSec, maxSec + 1)
        return delaySec * 1000L
    }
}
