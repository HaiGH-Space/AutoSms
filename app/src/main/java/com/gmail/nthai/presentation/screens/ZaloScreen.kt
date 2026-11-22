package com.gmail.nthai.presentation.screens

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gmail.nthai.presentation.components.ExcelImportDialogs
import com.gmail.nthai.presentation.components.common.CampaignTabItem
import com.gmail.nthai.presentation.components.common.FeatureCampaignScaffold
import com.gmail.nthai.presentation.components.zalo.ZaloConfigTab
import com.gmail.nthai.presentation.components.zalo.ZaloProgressTab
import com.gmail.nthai.presentation.components.zalo.ZaloRecipientTab
import com.gmail.nthai.presentation.viewmodel.ZaloViewModel

@Composable
fun ZaloScreen(
    initialTab: Int = 0,
    showTopBarBackIcon: Boolean = false,
    onBack: () -> Unit = {},
    viewModel: ZaloViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }

    val recipients by viewModel.recipients.collectAsState()
    val blacklistedPhones by viewModel.blacklistedPhones.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val progressState by viewModel.progressState.collectAsState()
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val selectedTemplateId by viewModel.selectedTemplateId.collectAsState()
    val messageContent by viewModel.messageContent.collectAsState()
    val delayRange by viewModel.delayRange.collectAsState()
    val autoAddFriend by viewModel.autoAddFriend.collectAsState()
    val sendOnlyIfUnsent by viewModel.sendOnlyIfUnsent.collectAsState()
    val isLowSpecDevice by viewModel.isLowSpecDevice.collectAsState()

    val pendingImportState by viewModel.pendingImportState.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val importLoadingMessage by viewModel.importLoadingMessage.collectAsState()

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
            viewModel.prepareExcelImportWithUri(context, uri) { err ->
                Toast.makeText(context, "Lỗi đọc tiêu đề Excel Zalo: ${err.message}", Toast.LENGTH_LONG).show()
            }
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

    val tabs = listOf(
        CampaignTabItem("Soạn tin & Cấu hình", Icons.Default.Tune),
        CampaignTabItem("Danh sách SĐT", Icons.Default.People),
        CampaignTabItem("Tiến độ gửi", Icons.AutoMirrored.Filled.Send)
    )

    FeatureCampaignScaffold(
        title = "Tự động gửi Zalo",
        tabs = tabs,
        selectedTabIndex = selectedTab,
        onTabSelected = { selectedTab = it },
        navigationIcon = if (showTopBarBackIcon) {
            {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color(0xFF171717)
                    )
                }
            }
        } else null
    ) { currentTab ->
        when (currentTab) {
            0 -> ZaloConfigTab(
                isAccessibilityEnabled = isAccessibilityEnabled,
                templates = templates,
                selectedTemplateId = selectedTemplateId,
                onSelectTemplate = { viewModel.selectTemplate(it) },
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
                    if (!isAccessibilityEnabled) {
                        Toast.makeText(
                            context,
                            "Vui lòng cấp quyền Trợ năng Zalo trước khi bắt đầu gửi tin!",
                            Toast.LENGTH_LONG
                        ).show()
                        openAccessibilitySettings()
                    } else {
                        viewModel.startCampaign()
                        Toast.makeText(
                            context,
                            "Đã khởi chạy chiến dịch gửi Zalo tự động!",
                            Toast.LENGTH_SHORT
                        ).show()
                        selectedTab = 2
                    }
                },
                onNextToRecipients = {
                    selectedTab = 1
                },
                onImportExcel = {
                    filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                },
                onOpenAccessibilitySettings = openAccessibilitySettings
            )

            1 -> ZaloRecipientTab(
                isAccessibilityEnabled = isAccessibilityEnabled,
                recipients = recipients,
                blacklistedPhones = blacklistedPhones,
                onAddRecipient = { name, phone -> viewModel.addRecipient(name, phone) },
                onDeleteRecipient = { id -> viewModel.deleteRecipient(id) },
                onAddToBlacklist = { phone -> viewModel.addToBlacklist(phone) },
                onRetryRecipient = { id -> viewModel.resetRecipientStatus(id) },
                onImportExcel = {
                    filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                },
                onStartSending = {
                    if (!isAccessibilityEnabled) {
                        Toast.makeText(
                            context,
                            "Vui lòng cấp quyền Trợ năng Zalo trước khi bắt đầu gửi tin!",
                            Toast.LENGTH_LONG
                        ).show()
                        openAccessibilitySettings()
                    } else {
                        viewModel.startCampaign()
                        Toast.makeText(
                            context,
                            "Đã khởi chạy chiến dịch gửi Zalo tự động!",
                            Toast.LENGTH_SHORT
                        ).show()
                        selectedTab = 2
                    }
                },
                onOpenAccessibilitySettings = openAccessibilitySettings
            )

            2 -> ZaloProgressTab(
                progressState = progressState,
                logs = logs,
                onPauseCampaign = { viewModel.pauseCampaign() },
                onResumeCampaign = { viewModel.resumeCampaign() },
                onStopCampaign = { viewModel.stopCampaign() },
                onClearLogs = { viewModel.clearLogs() }
            )
        }
    }

    ExcelImportDialogs(
        isImporting = isImporting,
        importLoadingMessage = importLoadingMessage,
        pendingImportState = pendingImportState,
        onSelectHeader = { viewModel.updateSelectedImportHeader(it) },
        onSelectImportMode = { viewModel.updateSelectedImportMode(it) },
        onConfirmImport = {
            viewModel.confirmImport { result ->
                result.onSuccess { count ->
                    Toast.makeText(context, "Đã nhập thành công $count SĐT Zalo", Toast.LENGTH_SHORT).show()
                    selectedTab = 1
                }.onFailure { err ->
                    Toast.makeText(context, "Lỗi nhập Excel: ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        },
        onDismissImport = { viewModel.cancelImport() }
    )
}

@Preview(showBackground = true)
@Composable
fun ZaloScreenPreview() {
    MaterialTheme {
        ZaloScreen()
    }
}
