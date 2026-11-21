package com.gmail.nthai.domain.engine

import com.gmail.nthai.domain.model.AutoCallConfig
import com.gmail.nthai.domain.model.AutoCallProgress
import com.gmail.nthai.domain.model.AutoCallStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoCallEngineTest {

    @Test
    fun autoCallProgress_initialState_isIdle() {
        val initialProgress = AutoCallProgress()
        assertEquals(AutoCallStatus.IDLE, initialProgress.status)
        assertEquals(0, initialProgress.totalCount)
        assertEquals(0, initialProgress.completedCount)
        assertFalse(initialProgress.isWaitingDelay)
    }

    @Test
    fun autoCallConfig_defaultValues_areCorrect() {
        val defaultConfig = AutoCallConfig()
        assertEquals(5, defaultConfig.delaySeconds)
        assertTrue(defaultConfig.selectedCustomerIds.isEmpty())
        assertEquals(1, defaultConfig.simSlot)
    }

    @Test
    fun autoCallConfig_simSlotSelection_isRespected() {
        val configSlot2 = AutoCallConfig(simSlot = 2, delaySeconds = 10)
        assertEquals(2, configSlot2.simSlot)
        assertEquals(10, configSlot2.delaySeconds)
    }

    @Test
    fun autoCallProgress_copyState_updatesCorrectly() {
        val progress = AutoCallProgress(
            status = AutoCallStatus.RUNNING,
            totalCount = 10,
            completedCount = 3,
            currentCustomerName = "Test Customer",
            currentPhone = "0987654321",
            countdownRemainingSeconds = 5,
            isWaitingDelay = true
        )

        val updated = progress.copy(
            completedCount = 4,
            isWaitingDelay = false
        )

        assertEquals(AutoCallStatus.RUNNING, updated.status)
        assertEquals(10, updated.totalCount)
        assertEquals(4, updated.completedCount)
        assertFalse(updated.isWaitingDelay)
        assertEquals("Test Customer", updated.currentCustomerName)
    }
}
