package com.gmail.nthai.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Tổng quan", Icons.Default.Dashboard)
    data object Customers :
        Screen("customers?simSlot={simSlot}", "Khách hàng", Icons.Default.People) {
        const val BASE_ROUTE = "customers"
        fun route(simSlot: Int = 1) = "customers?simSlot=$simSlot"
    }

    data object Progress : Screen("progress", "Gửi tin", Icons.AutoMirrored.Filled.Send)
    data object Management : Screen("management", "Quản lý", Icons.Default.Tune)

    data object AutoSms : Screen("auto_sms", "Gửi SMS", Icons.AutoMirrored.Filled.Send)
    data object AutoZalo : Screen("auto_zalo", "Gửi Zalo", Icons.AutoMirrored.Filled.Chat)
    data object Report : Screen("report", "Báo cáo", Icons.Default.Assessment)
}
