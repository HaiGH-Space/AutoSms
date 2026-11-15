package com.gmail.nthai.presentation.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verify_bottomNavigation_containsFourItems() {
        composeTestRule.setContent {
            AppNavHost()
        }

        composeTestRule.onNodeWithText("Tổng quan").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gửi SMS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gửi Zalo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quản lý").assertIsDisplayed()
    }

    @Test
    fun navigateToAutoZalo_displaysZaloScreen() {
        composeTestRule.setContent {
            AppNavHost()
        }

        composeTestRule.onNodeWithText("Gửi Zalo").performClick()
        composeTestRule.onNodeWithText("Tự động gửi Zalo").assertIsDisplayed()
    }

    @Test
    fun navigateToManagement_displaysManagementScreen() {
        composeTestRule.setContent {
            AppNavHost()
        }

        composeTestRule.onNodeWithText("Quản lý").performClick()
        composeTestRule.onNodeWithText("Quản lý & Cấu hình").assertIsDisplayed()
    }

    @Test
    fun navigateToSms_allowsSelectingSimTwo() {
        composeTestRule.setContent {
            AppNavHost()
        }

        composeTestRule.onNodeWithText("Gửi SMS").performClick()
        composeTestRule.onNodeWithText("SIM 1").assertIsSelected()
        composeTestRule.onNodeWithText("SIM 2").performClick().assertIsSelected()
    }

    @Test
    fun navigateHomeFromSms_returnsToDashboard() {
        composeTestRule.setContent {
            AppNavHost()
        }

        composeTestRule.onNodeWithText("Gửi SMS").performClick()
        composeTestRule.onNodeWithText("Tự động gửi SMS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tổng quan").performClick()
        composeTestRule.onNodeWithText("Tổng quan").assertIsDisplayed()
    }
}
