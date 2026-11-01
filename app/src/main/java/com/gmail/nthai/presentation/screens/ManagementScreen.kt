package com.gmail.nthai.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmail.nthai.presentation.viewmodel.ConfigViewModel
import com.gmail.nthai.presentation.viewmodel.UpdateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementScreen(
    configViewModel: ConfigViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        Triple("Mẫu tin", "Mẫu tin nhắn", Icons.Outlined.Sms),
        Triple("SIM", "Quản lý SIM", Icons.Outlined.SimCard),
        Triple("Quy tắc", "Quy định & Cập nhật", Icons.Outlined.Settings)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quản lý hệ thống",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val selected = selectedTab == index
                        val containerColor by animateColorAsState(
                            targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            label = "managementTabColor"
                        )
                        val contentColor by animateColorAsState(
                            targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            label = "managementTabContentColor"
                        )
                        Surface(
                            onClick = { selectedTab = index },
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    this.selected = selected
                                    role = Role.Tab
                                    contentDescription = tab.second
                                },
                            color = containerColor,
                            contentColor = contentColor,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(tab.third, contentDescription = null)
                                Text(
                                    tab.first,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> TemplateEditorScreen(showTopBar = false)
                1 -> SimManagementTab(viewModel = configViewModel, showTopBar = false)
                2 -> RuleAndUpdateTab(viewModel = configViewModel, updateViewModel = updateViewModel, showTopBar = false)
            }
        }
    }
}
