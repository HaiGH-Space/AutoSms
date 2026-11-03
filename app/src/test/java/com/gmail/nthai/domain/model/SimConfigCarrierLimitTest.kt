package com.gmail.nthai.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SimConfigCarrierLimitTest {

    @Test
    fun `getDefaultDailyLimit returns correct carrier limits`() {
        assertEquals(40, SimConfig.getDefaultDailyLimit("Viettel"))
        assertEquals(40, SimConfig.getDefaultDailyLimit("VIETTEL TELECOM"))
        assertEquals(50, SimConfig.getDefaultDailyLimit("VinaPhone"))
        assertEquals(50, SimConfig.getDefaultDailyLimit("MobiFone"))
        assertEquals(100, SimConfig.getDefaultDailyLimit("Vietnamobile"))
        assertEquals(50, SimConfig.getDefaultDailyLimit("Gmobile"))
        assertEquals(50, SimConfig.getDefaultDailyLimit("Wintel"))
        assertEquals(50, SimConfig.getDefaultDailyLimit("iTel"))
        assertEquals(40, SimConfig.getDefaultDailyLimit(""))
        assertEquals(40, SimConfig.getDefaultDailyLimit("Unknown"))
    }

    @Test
    fun `SimConfig constructor defaults dailyLimit based on carrierName`() {
        val viettelSim = SimConfig(subscriptionId = 1, simSlot = 1, carrierName = "Viettel")
        assertEquals(40, viettelSim.dailyLimit)

        val vietnamobileSim = SimConfig(subscriptionId = 1, simSlot = 1, carrierName = "Vietnamobile")
        assertEquals(100, vietnamobileSim.dailyLimit)

        val vinaSim = SimConfig(subscriptionId = 2, simSlot = 2, carrierName = "VinaPhone")
        assertEquals(50, vinaSim.dailyLimit)
    }
}
