package com.gmail.nthai.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmail.nthai.presentation.components.config.RuleAndUpdateTab
import com.gmail.nthai.presentation.components.config.SimManagementTab
import com.gmail.nthai.presentation.viewmodel.ConfigViewModel
import com.gmail.nthai.presentation.viewmodel.UpdateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementScreen(
    initialTab: Int = 0,
    configViewModel: ConfigViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 3)) }
    val tabs = listOf(
        Triple("Mẫu tin", "Mẫu tin nhắn & Spintax", Icons.Outlined.Sms),
        Triple("Báo cáo", "Báo cáo gửi & Xuất Excel", Icons.Outlined.Assessment),
        Triple("Hạn mức SIM", "Quản lý 2 khe SIM", Icons.Outlined.SimCard),
        Triple("Quy tắc & Update", "Quy định an toàn & Cập nhật", Icons.Outlined.Settings)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quản lý & Cấu hình",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF171717)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFFFFF)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFAFAFA))
        ) {
            SecondaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFFFFFFF),
                contentColor = Color(0xFF171717),
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(selectedTab),
                        color = Color(0xFF171717)
                    )
                },
                edgePadding = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEBEBEB))
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = selectedTab == index
                    Tab(
                        selected = selected,
                        onClick = { selectedTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    tab.third,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selected) Color(0xFF171717) else Color(0xFF888888)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    tab.first,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color(0xFF171717) else Color(0xFF888888)
                                )
                            }
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> TemplateEditorScreen(showTopBar = false)
                1 -> ReportScreen(showTopBar = false)
                2 -> SimManagementTab(viewModel = configViewModel, showTopBar = false)
                3 -> RuleAndUpdateTab(viewModel = configViewModel, updateViewModel = updateViewModel, showTopBar = false)
            }
        }
    }
}
