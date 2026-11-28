package com.gmail.nthai.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmail.nthai.presentation.components.ExcelImportDialogs
import com.gmail.nthai.presentation.components.autocall.CallConfigTab
import com.gmail.nthai.presentation.components.autocall.CallProgressTab
import com.gmail.nthai.presentation.components.autocall.CallRecipientTab
import com.gmail.nthai.presentation.components.common.CampaignTabItem
import com.gmail.nthai.presentation.components.common.FeatureCampaignScaffold

@Composable
fun AutoCallScreen(
    viewModel: AutoCallViewModel = hiltViewModel(),
    initialTab: Int = 0,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }

    val progressState by viewModel.progressState.collectAsState()
    val callLogs by viewModel.callLogs.collectAsState()
    val recipients by viewModel.recipients.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val blacklistedPhones by viewModel.blacklistedPhones.collectAsState()
    val delaySeconds by viewModel.delaySeconds.collectAsState()
    val simConfigs by viewModel.simConfigs.collectAsState()
    val selectedSimSlot by viewModel.selectedSimSlot.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    val pendingImportState by viewModel.pendingImportState.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val importLoadingMessage by viewModel.importLoadingMessage.collectAsState()

    var hasCallPermission by remember {
        mutableStateOf(
            (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED)
        )
    }

    var hasOverlayPermission by remember {
        mutableStateOf(android.provider.Settings.canDrawOverlays(context))
    }

    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        hasCallPermission =
            (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED)
        hasOverlayPermission = android.provider.Settings.canDrawOverlays(context)
        onPauseOrDispose {}
    }

    val requestOverlayPermissionAction = {
        try {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val handleStartCampaign = {
        if (recipients.isEmpty()) {
            Toast.makeText(context, "Vui lòng thêm danh bạ gọi trước khi bắt đầu", Toast.LENGTH_SHORT).show()
        } else if (hasCallPermission) {
            viewModel.startCampaign()
            Toast.makeText(context, "Đã khởi chạy chiến dịch gọi tự động!", Toast.LENGTH_SHORT).show()
            selectedTab = 2
        } else {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val callGranted = permissions[Manifest.permission.CALL_PHONE] ?: false
        val stateGranted = permissions[Manifest.permission.READ_PHONE_STATE] ?: false
        val logGranted = permissions[Manifest.permission.READ_CALL_LOG] ?: false
        hasCallPermission = callGranted && stateGranted && logGranted
        if (hasCallPermission) {
            viewModel.startCampaign()
            Toast.makeText(context, "Đã khởi chạy chiến dịch gọi tự động!", Toast.LENGTH_SHORT).show()
            selectedTab = 2
        } else {
            Toast.makeText(context, "Vui lòng cấp quyền gọi điện và nhật ký cuộc gọi trong Cài đặt ứng dụng", Toast.LENGTH_LONG).show()
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val requestPermissionAction = {
        val callGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        val stateGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        val logGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        if (callGranted && stateGranted && logGranted) {
            hasCallPermission = true
        } else {
            try {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG
                    )
                )
            } catch (_: Exception) {
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.prepareExcelImportWithUri(context, it) { err ->
                Toast.makeText(context, "Lỗi đọc tiêu đề Excel: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val tabs = listOf(
        CampaignTabItem("Soạn & Cấu hình", Icons.Default.Tune),
        CampaignTabItem("Danh sách gọi", Icons.Default.People),
        CampaignTabItem("Tiến độ gọi", Icons.AutoMirrored.Filled.Send)
    )

    FeatureCampaignScaffold(
        title = "Tự động gọi điện",
        tabs = tabs,
        selectedTabIndex = selectedTab,
        onTabSelected = { selectedTab = it },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF171717)
                )
            }
        },
        actions = {
            if (selectedTab == 1 && recipients.isNotEmpty()) {
                IconButton(onClick = { viewModel.resetStatus() }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Đặt lại trạng thái",
                        tint = Color(0xFF888888)
                    )
                }
                IconButton(onClick = { viewModel.deleteAllRecipients() }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa tất cả",
                        tint = Color(0xFFEE0000)
                    )
                }
            }
        }
    ) { currentTab ->
        when (currentTab) {
            0 -> {
                CallConfigTab(
                    hasCallPermission = hasCallPermission,
                    onRequestPermission = requestPermissionAction,
                    hasOverlayPermission = hasOverlayPermission,
                    onRequestOverlayPermission = requestOverlayPermissionAction,
                    delaySeconds = delaySeconds,
                    onDelayChange = viewModel::updateDelay,
                    selectedSimSlot = selectedSimSlot,
                    onSimSlotChange = viewModel::updateSimSlot,
                    simConfigs = simConfigs,
                    totalRecipientsCount = recipients.size,
                    selectedRecipientsCount = stats.selectedCount,
                    onNextToRecipients = { selectedTab = 1 },
                    onImportExcel = {
                        filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    }
                )
            }
            1 -> {
                CallRecipientTab(
                    recipients = recipients,
                    stats = stats,
                    blacklistedPhones = blacklistedPhones,
                    searchQuery = searchQuery,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    selectedFilter = selectedFilter,
                    onFilterChange = viewModel::updateFilter,
                    onToggleSelection = viewModel::toggleRecipientSelection,
                    onSelectAll = viewModel::selectAllRecipients,
                    onAddRecipient = viewModel::addRecipient,
                    onDeleteRecipient = viewModel::deleteRecipient,
                    onDeleteAllRecipients = viewModel::deleteAllRecipients,
                    onResetStatus = viewModel::resetStatus,
                    onAddToBlacklist = viewModel::addToBlacklist,
                    onImportExcel = {
                        filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    },
                    selectedSimSlot = selectedSimSlot,
                    onStartCampaign = handleStartCampaign
                )
            }
            2 -> {
                CallProgressTab(
                    progressState = progressState,
                    callLogs = callLogs,
                    selectedSimSlot = selectedSimSlot,
                    onPauseCampaign = viewModel::pauseCampaign,
                    onResumeCampaign = viewModel::resumeCampaign,
                    onStopCampaign = viewModel::stopCampaign,
                    onClearCallLogs = viewModel::clearCallLogs
                )
            }
        }
    }

    // Excel Import Dialogs
    ExcelImportDialogs(
        isImporting = isImporting,
        importLoadingMessage = importLoadingMessage,
        pendingImportState = pendingImportState,
        onSelectHeader = viewModel::updateSelectedImportHeader,
        onSelectNameHeader = viewModel::updateSelectedImportNameHeader,
        onSelectImportMode = viewModel::updateSelectedImportMode,
        onConfirmImport = {
            viewModel.confirmImport { result ->
                result.onSuccess { count ->
                    Toast.makeText(context, "Nhập thành công $count khách hàng", Toast.LENGTH_SHORT).show()
                    selectedTab = 1
                }.onFailure { err ->
                    Toast.makeText(context, "Lỗi nhập Excel: ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        },
        onDismissImport = viewModel::cancelImport
    )
}
