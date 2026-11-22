package com.gmail.nthai.presentation.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.gmail.nthai.presentation.components.common.CampaignTabItem
import com.gmail.nthai.presentation.components.common.FeatureCampaignScaffold

@Composable
fun AutoSmsScreen(
    initialTab: Int = 0,
    simSlot: Int = 1
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }
    var selectedSimSlot by rememberSaveable(simSlot) { mutableIntStateOf(simSlot.coerceIn(1, 2)) }

    val tabs = listOf(
        CampaignTabItem("Danh sách khách hàng", Icons.Default.People),
        CampaignTabItem("Tiến độ gửi SMS", Icons.AutoMirrored.Filled.Send)
    )

    FeatureCampaignScaffold(
        title = "Tự động gửi SMS",
        tabs = tabs,
        selectedTabIndex = selectedTab,
        onTabSelected = { selectedTab = it }
    ) { currentTab ->
        when (currentTab) {
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

@Preview(showBackground = true)
@Composable
fun AutoSmsScreenPreview() {
    MaterialTheme {
        AutoSmsScreen()
    }
}
