package com.gmail.nthai.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DetectVietnameseCarrierUseCaseTest {

    @Test
    fun `detects common Vietnamese mobile prefixes`() {
        assertEquals("Viettel", DetectVietnameseCarrierUseCase.detect("0981234567"))
        assertEquals("VinaPhone", DetectVietnameseCarrierUseCase.detect("0911234567"))
        assertEquals("MobiFone", DetectVietnameseCarrierUseCase.detect("0901234567"))
        assertEquals("Vietnamobile", DetectVietnameseCarrierUseCase.detect("0921234567"))
        assertEquals("Gmobile", DetectVietnameseCarrierUseCase.detect("0591234567"))
        assertEquals("Wintel", DetectVietnameseCarrierUseCase.detect("0551234567"))
        assertEquals("iTel", DetectVietnameseCarrierUseCase.detect("0871234567"))
    }

    @Test
    fun `normalizes country code and formatting`() {
        assertEquals("VinaPhone", DetectVietnameseCarrierUseCase.detect("+84 912 345 678"))
    }

    @Test
    fun `returns unknown for unsupported or invalid numbers`() {
        assertNull(DetectVietnameseCarrierUseCase.detect("0123456789"))
        assertNull(DetectVietnameseCarrierUseCase.detect("123"))
    }
}
