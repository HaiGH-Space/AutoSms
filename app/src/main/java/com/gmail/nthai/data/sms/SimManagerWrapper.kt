package com.gmail.nthai.data.sms

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionManager
import com.gmail.nthai.domain.model.SimConfig

class SimManagerWrapper(
    private val context: Context
) {

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun getActiveSims(): List<SimConfig> {
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            ?: return emptyList()

        val result = mutableListOf<SimConfig>()
        val takenSlots = mutableSetOf<Int>()

        // Layer 1: activeSubscriptionInfoList
        val infoList = try {
            subscriptionManager.activeSubscriptionInfoList
        } catch (_: Exception) {
            null
        } ?: emptyList()

        for (info in infoList) {
            val slot = info.simSlotIndex + 1
            if (slot in 1..2 && slot !in takenSlots) {
                takenSlots.add(slot)
                result.add(
                    SimConfig(
                        subscriptionId = info.subscriptionId,
                        simSlot = slot,
                        dailyLimit = 100
                    )
                )
            }
        }

        // Handle eSIM or unknown slot index in activeSubscriptionInfoList
        for (info in infoList) {
            val slot = info.simSlotIndex + 1
            if (slot !in 1..2) {
                val availableSlot = (1..2).firstOrNull { it !in takenSlots }
                if (availableSlot != null) {
                    takenSlots.add(availableSlot)
                    result.add(
                        SimConfig(
                            subscriptionId = info.subscriptionId,
                            simSlot = availableSlot,
                            dailyLimit = 100
                        )
                    )
                }
            }
        }

        // Layer 2: getActiveSubscriptionInfoForSimSlotIndex(0) and (1)
        for (slotIndex in 0..1) {
            val simSlot = slotIndex + 1
            if (simSlot !in takenSlots) {
                val info = try {
                    subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(slotIndex)
                } catch (_: Exception) {
                    null
                }
                if (info != null && info.subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    takenSlots.add(simSlot)
                    result.add(
                        SimConfig(
                            subscriptionId = info.subscriptionId,
                            simSlot = simSlot,
                            dailyLimit = 100
                        )
                    )
                }
            }
        }

        // Layer 3: getSubscriptionIds(0) and (1) on Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            for (slotIndex in 0..1) {
                val simSlot = slotIndex + 1
                if (simSlot !in takenSlots) {
                    val subIds = try {
                        subscriptionManager.getSubscriptionIds(slotIndex)
                    } catch (_: Exception) {
                        null
                    }
                    if (subIds != null && subIds.isNotEmpty() && subIds[0] != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                        takenSlots.add(simSlot)
                        result.add(
                            SimConfig(
                                subscriptionId = subIds[0],
                                simSlot = simSlot,
                                dailyLimit = 100
                            )
                        )
                    }
                }
            }
        }

        // Layer 4: Default SMS Subscription ID fallback
        if (result.isEmpty()) {
            val defaultSmsSubId = SubscriptionManager.getDefaultSmsSubscriptionId()
            val validSubId = if (defaultSmsSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                defaultSmsSubId
            } else {
                val defaultSubId = SubscriptionManager.getDefaultSubscriptionId()
                if (defaultSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) defaultSubId else -1
            }

            if (validSubId != -1) {
                result.add(
                    SimConfig(
                        subscriptionId = validSubId,
                        simSlot = 1,
                        dailyLimit = 100
                    )
                )
            }
        }

        return result.sortedBy { it.simSlot }
    }

    @SuppressLint("MissingPermission")
    fun getSubscriptionIdForSlot(simSlot: Int): Int? {
        val activeSims = getActiveSims()
        return activeSims.find { it.simSlot == simSlot }?.subscriptionId
    }
}
