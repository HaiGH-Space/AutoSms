package com.gmail.nthai.presentation.screens

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gmail.nthai.presentation.components.zalo.ZaloConfigTab
import com.gmail.nthai.presentation.components.zalo.ZaloProgressTab
import com.gmail.nthai.presentation.components.zalo.ZaloRecipientTab
import com.gmail.nthai.presentation.viewmodel.ZaloViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZaloScreen(
    initialTab: Int = 0,
    showTopBarBackIcon: Boolean = false,
    onBack: () -> Unit = {},
    viewModel: ZaloViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    val recipients by viewModel.recipients.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val progressState by viewModel.progressState.collectAsState()
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsState()
    val messageContent by viewModel.messageContent.collectAsState()
    val delayRange by viewModel.delayRange.collectAsState()
    val autoAddFriend by viewModel.autoAddFriend.collectAsState()
    val sendOnlyIfUnsent by viewModel.sendOnlyIfUnsent.collectAsState()
    val isLowSpecDevice by viewModel.isLowSpecDevice.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.checkAccessibilityStatus(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAccessibilityStatus(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importExcelFile(context, uri)
            Toast.makeText(context, "Đã nhập file Excel Zalo", Toast.LENGTH_SHORT).show()
            selectedTab = 1 // Show in recipient list tab
        }
    }

    val openAccessibilitySettings = {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Không thể mở Cài đặt Trợ năng: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Tự động gửi Zalo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF171717)
                        )
                    },
                    navigationIcon = {
                        if (showTopBarBackIcon) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Quay lại",
                                    tint = Color(0xFF171717)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFFFFFF)
                    )
                )
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color(0xFF171717),
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(selectedTab),
                            color = Color(0xFF171717)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEBEBEB))
                ) {
                    val tabs = listOf(
                        Pair("Soạn tin & Cấu hình", Icons.Default.Tune),
                        Pair("Danh sách SĐT", Icons.Default.People),
                        Pair("Tiến độ gửi", Icons.AutoMirrored.Filled.Send)
                    )
                    tabs.forEachIndexed { index, tab ->
                        val selected = selectedTab == index
                        Tab(
                            selected = selected,
                            onClick = { selectedTab = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        tab.second,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (selected) Color(0xFF171717) else Color(0xFF888888)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tab.first,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) Color(0xFF171717) else Color(
                                            0xFF888888
                                        )
                                    )
                                }
                            }
                        )
                    }
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
                0 -> ZaloConfigTab(
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    messageContent = messageContent,
                    onMessageContentChange = { viewModel.messageContent.value = it },
                    delayRange = delayRange,
                    onDelayRangeChange = { viewModel.delayRange.value = it },
                    autoAddFriend = autoAddFriend,
                    onAutoAddFriendChange = { viewModel.autoAddFriend.value = it },
                    sendOnlyIfUnsent = sendOnlyIfUnsent,
                    onSendOnlyIfUnsentChange = { viewModel.sendOnlyIfUnsent.value = it },
                    isLowSpecDevice = isLowSpecDevice,
                    onIsLowSpecDeviceChange = { viewModel.isLowSpecDevice.value = it },
                    onStartSending = {
                        viewModel.startCampaign()
                        Toast.makeText(
                            context,
                            "Đã khởi chạy chiến dịch gửi Zalo tự động!",
                            Toast.LENGTH_SHORT
                        ).show()
                        selectedTab = 2 // Switch to progress tab
                    },
                    onNextToRecipients = {
                        selectedTab = 1 // Switch to recipient list tab
                    },
                    onImportExcel = {
                        filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    },
                    onOpenAccessibilitySettings = openAccessibilitySettings
                )

                1 -> ZaloRecipientTab(
                    recipients = recipients,
                    onAddRecipient = { name, phone -> viewModel.addRecipient(name, phone) },
                    onDeleteRecipient = { id -> viewModel.deleteRecipient(id) },
                    onRetryRecipient = { id -> viewModel.resetRecipientStatus(id) },
                    onImportExcel = {
                        filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    },
                    onStartSending = {
                        viewModel.startCampaign()
                        Toast.makeText(
                            context,
                            "Đã khởi chạy chiến dịch gửi Zalo tự động!",
                            Toast.LENGTH_SHORT
                        ).show()
                        selectedTab = 2 // Switch to progress tab
                    }
                )

                2 -> ZaloProgressTab(
                    progressState = progressState,
                    logs = logs,
                    onPauseCampaign = { viewModel.pauseCampaign() },
                    onResumeCampaign = { viewModel.resumeCampaign() },
                    onStopCampaign = { viewModel.stopCampaign() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ZaloScreenPreview() {
    MaterialTheme {
        ZaloScreen()
    }
}
