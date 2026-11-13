package com.gmail.nthai.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Trang chủ", Icons.Default.Home)
    data object AutoSms : Screen("auto_sms", "Gửi SMS", Icons.AutoMirrored.Filled.Send)
    data object AutoZalo : Screen("auto_zalo", "Gửi Zalo", Icons.AutoMirrored.Filled.Chat)
    data object Management : Screen("management", "Quản lý", Icons.Default.Tune)

    data object Customers : Screen("customers/{simSlot}", "Danh sách", Icons.Default.People) {
        fun route(simSlot: Int) = "customers/$simSlot"
    }
    data object Progress : Screen("progress", "Gửi tin", Icons.AutoMirrored.Filled.Send)
    data object Report : Screen("report", "Báo cáo", Icons.Default.Assessment)
}
