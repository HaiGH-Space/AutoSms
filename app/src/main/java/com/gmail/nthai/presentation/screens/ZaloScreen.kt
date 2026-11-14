package com.gmail.nthai.presentation.screens

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmail.nthai.presentation.components.zalo.ZaloConfigTab
import com.gmail.nthai.presentation.components.zalo.ZaloProgressTab
import com.gmail.nthai.presentation.components.zalo.ZaloRecipientTab
import com.gmail.nthai.presentation.viewmodel.ZaloViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZaloScreen(
    initialTab: Int = 0,
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
    val selectedImages by viewModel.selectedImages.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkAccessibilityStatus(context)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importExcelFile(context, uri)
            Toast.makeText(context, "Đã nhập file Excel Zalo", Toast.LENGTH_SHORT).show()
            selectedTab = 0 // Show in recipient list
        }
    }

    val openAccessibilitySettings = {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Không thể mở Cài đặt Trợ năng: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Tự động gửi Zalo",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Danh sách SĐT",
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(Icons.Default.People, contentDescription = "Danh sách SĐT") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Tiến độ gửi",
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Tiến độ") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = "Cấu hình",
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(Icons.Default.Tune, contentDescription = "Cấu hình") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ZaloRecipientTab(
                    recipients = recipients,
                    onAddRecipient = { name, phone -> viewModel.addRecipient(name, phone) },
                    onDeleteRecipient = { id -> viewModel.deleteRecipient(id) },
                    onRetryRecipient = { id -> viewModel.resetRecipientStatus(id) },
                    onImportExcel = {
                        filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    }
                )
                1 -> ZaloProgressTab(
                    progressState = progressState,
                    logs = logs,
                    onPauseCampaign = { viewModel.pauseCampaign() },
                    onResumeCampaign = { viewModel.resumeCampaign() },
                    onStopCampaign = { viewModel.stopCampaign() }
                )
                2 -> ZaloConfigTab(
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    messageContent = messageContent,
                    onMessageContentChange = { viewModel.messageContent.value = it },
                    delayRange = delayRange,
                    onDelayRangeChange = { viewModel.delayRange.value = it },
                    autoAddFriend = autoAddFriend,
                    onAutoAddFriendChange = { viewModel.autoAddFriend.value = it },
                    sendOnlyIfUnsent = sendOnlyIfUnsent,
                    onSendOnlyIfUnsentChange = { viewModel.sendOnlyIfUnsent.value = it },
                    selectedImages = selectedImages,
                    onAddImage = { viewModel.addSelectedImage(it) },
                    onRemoveImage = { viewModel.removeSelectedImage(it) },
                    onStartSending = {
                        viewModel.startCampaign()
                        Toast.makeText(context, "Đã khởi chạy chiến dịch gửi Zalo tự động!", Toast.LENGTH_SHORT).show()
                        selectedTab = 1 // Switch to progress tab
                    },
                    onImportExcel = {
                        filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    },
                    onOpenAccessibilitySettings = openAccessibilitySettings
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
