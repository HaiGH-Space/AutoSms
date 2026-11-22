package com.gmail.nthai.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class CampaignTabItem(
    val title: String,
    val icon: ImageVector? = null,
    val badgeCount: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureCampaignScaffold(
    title: String,
    tabs: List<CampaignTabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    containerColor: Color = Color(0xFFFAFAFA),
    topBarContainerColor: Color = Color(0xFFFFFFFF),
    content: @Composable (selectedTabIndex: Int) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                AutoSmsTopAppBar(
                    title = title,
                    subtitle = subtitle,
                    navigationIcon = navigationIcon,
                    leadingContent = leadingContent,
                    actions = actions,
                    containerColor = topBarContainerColor
                )
                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex.coerceIn(0, (tabs.size - 1).coerceAtLeast(0)),
                    containerColor = topBarContainerColor,
                    contentColor = Color(0xFF171717),
                    indicator = {
                        if (tabs.isNotEmpty()) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(selectedTabIndex.coerceIn(0, tabs.size - 1)),
                                color = Color(0xFF171717)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEBEBEB))
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val selected = selectedTabIndex == index
                        Tab(
                            selected = selected,
                            onClick = { onTabSelected(index) },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (tab.icon != null) {
                                        if (tab.badgeCount != null && tab.badgeCount > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge {
                                                        Text(text = tab.badgeCount.toString())
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = tab.title,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (selected) Color(0xFF171717) else Color(0xFF888888)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = tab.title,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (selected) Color(0xFF171717) else Color(0xFF888888)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) Color(0xFF171717) else Color(0xFF888888)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        },
        containerColor = containerColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(containerColor)
        ) {
            content(selectedTabIndex)
        }
    }
}
