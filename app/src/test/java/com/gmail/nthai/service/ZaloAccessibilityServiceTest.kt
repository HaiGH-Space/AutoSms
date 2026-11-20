package com.gmail.nthai.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZaloAccessibilityServiceTest {

    @Test
    fun testSendResultSealedClass() {
        val success: ZaloAccessibilityService.SendResult = ZaloAccessibilityService.SendResult.Success
        val notFound: ZaloAccessibilityService.SendResult = ZaloAccessibilityService.SendResult.NotFound("Not found")
        val error: ZaloAccessibilityService.SendResult = ZaloAccessibilityService.SendResult.Error("Timeout")

        assertTrue(success is ZaloAccessibilityService.SendResult.Success)
        assertEquals("Not found", (notFound as ZaloAccessibilityService.SendResult.NotFound).reason)
        assertEquals("Timeout", (error as ZaloAccessibilityService.SendResult.Error).reason)
    }

    @Test
    fun testPrepareAndClearAutoSendState() {
        ZaloAccessibilityService.prepareAutoSend(
            messageText = "Test Message",
            autoAddFriend = true,
            isLowSpecDevice = true
        )

        // Clear state
        ZaloAccessibilityService.clearPendingAction()
        ZaloAccessibilityService.cancelReturnToApp()
    }
}
