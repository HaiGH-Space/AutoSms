package com.gmail.nthai.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Trang chủ", Icons.Default.Home)
    data object Customers : Screen("customers/{simSlot}?importExcel={importExcel}", "Danh sách", Icons.Default.People) {
        fun route(simSlot: Int, importExcel: Boolean = false) =
            "customers/$simSlot?importExcel=$importExcel"
    }
    data object Template : Screen("template", "Mẫu tin", Icons.Default.EditNote)
    data object Config : Screen("config", "Cài đặt", Icons.Default.Settings)
    data object Progress : Screen("progress", "Gửi tin", Icons.Default.Send)
    data object Report : Screen("report", "Báo cáo", Icons.Default.Assessment)
}
