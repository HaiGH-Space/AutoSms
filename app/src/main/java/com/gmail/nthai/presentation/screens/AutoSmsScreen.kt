package com.gmail.nthai.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoSmsScreen(
    initialTab: Int = 0,
    simSlot: Int = 1
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var selectedSimSlot by remember(simSlot) { mutableIntStateOf(simSlot.coerceIn(1, 2)) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Tự động gửi SMS",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF171717)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFFFFFF)
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color(0xFF171717),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF171717)
                        )
                    },
                    modifier = Modifier.border(1.dp, Color(0xFFEBEBEB))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Danh sách khách hàng",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) Color(0xFF171717) else Color(
                                    0xFF888888
                                )
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.People,
                                contentDescription = "Danh sách khách hàng",
                                tint = if (selectedTab == 0) Color(0xFF171717) else Color(0xFF888888)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Tiến độ gửi SMS",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) Color(0xFF171717) else Color(
                                    0xFF888888
                                )
                            )
                        },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Tiến độ gửi SMS",
                                tint = if (selectedTab == 1) Color(0xFF171717) else Color(0xFF888888)
                            )
                        }
                    )
                }

            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFAFAFA))
        ) {
            when (selectedTab) {
                0 -> CustomerListScreen(
                    simSlot = selectedSimSlot,
                    showTopBar = false,
                    onBack = null
                )

                1 -> SendingProgressScreen(
                    showTopBar = false
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AutoSmsScreenPreview() {
    MaterialTheme {
        AutoSmsScreen()
    }
}
