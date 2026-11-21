package com.gmail.nthai.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmail.nthai.presentation.components.ExcelImportDialogs
import com.gmail.nthai.presentation.components.autocall.CallProgressConfigTab
import com.gmail.nthai.presentation.components.autocall.CallRecipientTab
import com.gmail.nthai.presentation.components.common.AutoSmsTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCallScreen(
    viewModel: AutoCallViewModel = hiltViewModel(),
    initialTab: Int = 0,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(initialTab) }

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
            ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        )
    }

    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        hasCallPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        onPauseOrDispose {}
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val callGranted = permissions[Manifest.permission.CALL_PHONE] ?: false
        val stateGranted = permissions[Manifest.permission.READ_PHONE_STATE] ?: false
        hasCallPermission = callGranted && stateGranted
        if (hasCallPermission) {
            viewModel.startCampaign()
        } else {
            Toast.makeText(context, "Vui lòng cấp quyền gọi điện trong Cài đặt ứng dụng", Toast.LENGTH_LONG).show()
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.prepareExcelImportWithUri(context, uri) { err ->
                Toast.makeText(context, "Lỗi đọc tiêu đề Excel: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                AutoSmsTopAppBar(
                    title = "Tự động gọi điện",
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
                        if (selectedTab == 0 && recipients.isNotEmpty()) {
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
                                "Danh sách gọi",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) Color(0xFF171717) else Color(0xFF888888)
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.People,
                                contentDescription = "Danh sách gọi",
                                tint = if (selectedTab == 0) Color(0xFF171717) else Color(0xFF888888)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Cấu hình & Tiến độ",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) Color(0xFF171717) else Color(0xFF888888)
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.Call,
                                contentDescription = "Tiến độ gọi",
                                tint = if (selectedTab == 1) Color(0xFF171717) else Color(0xFF888888)
                            )
                        }
                    )
                }
            }
        },
        containerColor = Color(0xFFFAFAFA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFAFAFA))
        ) {
            when (selectedTab) {
                0 -> {
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
                        }
                    )
                }
                1 -> {
                    CallProgressConfigTab(
                        context = context,
                        hasCallPermission = hasCallPermission,
                        onHasCallPermissionChange = { hasCallPermission = it },
                        permissionLauncher = permissionLauncher,
                        delaySeconds = delaySeconds,
                        onDelayChange = viewModel::updateDelay,
                        selectedSimSlot = selectedSimSlot,
                        onSimSlotChange = viewModel::updateSimSlot,
                        simConfigs = simConfigs,
                        totalRecipientsCount = recipients.size,
                        selectedRecipientsCount = stats.selectedCount,
                        progressState = progressState,
                        callLogs = callLogs,
                        onStartCampaign = viewModel::startCampaign,
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
            onSelectImportMode = viewModel::updateSelectedImportMode,
            onConfirmImport = {
                viewModel.confirmImport { result ->
                    result.onSuccess { count ->
                        Toast.makeText(context, "Nhập thành công $count khách hàng", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        Toast.makeText(context, "Lỗi nhập Excel: ${err.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDismissImport = viewModel::cancelImport
        )
    }
}
