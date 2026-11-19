package com.gmail.nthai.presentation.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.domain.model.UpdateState
import com.gmail.nthai.presentation.components.DownloadProgressDialog
import com.gmail.nthai.presentation.components.DownloadedInstallDialog
import com.gmail.nthai.presentation.components.LoadingDialog
import com.gmail.nthai.presentation.components.NoUpdateAvailableDialog
import com.gmail.nthai.presentation.components.SelectPhoneColumnDialog
import com.gmail.nthai.presentation.components.UpdateAvailableDialog
import com.gmail.nthai.presentation.components.UpdateErrorDialog
import com.gmail.nthai.presentation.components.common.AutoSmsTopAppBar
import com.gmail.nthai.presentation.components.customer.ResetStatusDialog
import com.gmail.nthai.presentation.components.home.ModernSimCard
import com.gmail.nthai.presentation.components.home.OverviewMetricsGrid
import com.gmail.nthai.presentation.components.home.WorkflowStepCard
import com.gmail.nthai.presentation.viewmodel.ConfigViewModel
import com.gmail.nthai.presentation.viewmodel.CustomerViewModel
import com.gmail.nthai.presentation.viewmodel.HomeDashboardViewModel
import com.gmail.nthai.presentation.viewmodel.UpdateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSelectSim: (Int) -> Unit,
    onNavigateToCustomers: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToManagement: (Int) -> Unit = {},
    onNavigateToAutoSms: () -> Unit = onNavigateToProgress,
    onNavigateToAutoZalo: () -> Unit = {},
    configViewModel: ConfigViewModel = hiltViewModel(),
    dashboardViewModel: HomeDashboardViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val updateState by updateViewModel.updateState.collectAsStateWithLifecycle()
    val simConfigs by configViewModel.simConfigs.collectAsStateWithLifecycle()
    val dashboardStats by dashboardViewModel.stats.collectAsStateWithLifecycle()
    val isSending by customerViewModel.isSending.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val pendingImportState by customerViewModel.pendingImportState.collectAsStateWithLifecycle()
    val isImporting by customerViewModel.isImporting.collectAsStateWithLifecycle()
    val importLoadingMessage by customerViewModel.importLoadingMessage.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            customerViewModel.prepareExcelImportWithUri(context, uri) { err ->
                Toast.makeText(context, "Lỗi đọc tiêu đề Excel: ${err.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    LaunchedEffect(Unit) {
        updateViewModel.checkForUpdate(isManualCheck = false)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                configViewModel.loadSims()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (val state = updateState) {
        is UpdateState.UpdateAvailable -> {
            UpdateAvailableDialog(
                updateInfo = state.updateInfo,
                onUpdateConfirm = { updateViewModel.startDownload(context, state.updateInfo) },
                onDismiss = { updateViewModel.dismissUpdate(state.updateInfo.versionCode) }
            )
        }

        is UpdateState.Downloading -> {
            DownloadProgressDialog(
                progressPercent = state.progressPercent,
                bytesDownloaded = state.bytesDownloaded,
                totalBytes = state.totalBytes,
                onCancel = { updateViewModel.cancelDownload() }
            )
        }

        is UpdateState.Downloaded -> {
            DownloadedInstallDialog(
                apkFile = state.apkFile,
                updateInfo = state.updateInfo,
                onInstall = { file ->
                    val installed = updateViewModel.installApk(context, file)
                    if (installed) updateViewModel.dismissState()
                },
                onDismiss = { updateViewModel.dismissState() }
            )
        }

        is UpdateState.NoUpdateAvailable -> {
            NoUpdateAvailableDialog(
                currentVersionName = state.currentVersionName,
                onDismiss = { updateViewModel.dismissState() }
            )
        }

        is UpdateState.Error -> {
            UpdateErrorDialog(
                errorMessage = state.message,
                onDismiss = { updateViewModel.dismissState() }
            )
        }

        else -> {}
    }

    val sim1Config = simConfigs.find { it.simSlot == 1 }
    val sim2Config = simConfigs.find { it.simSlot == 2 }

    Scaffold(
        topBar = {
            AutoSmsTopAppBar(
                title = "AutoSms",
                subtitle = "SMS Automation Platform",
                leadingContent = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF171717),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Outlined.Send,
                                contentDescription = null,
                                tint = Color(0xFFFFFFFF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                actions = {
                    if (isSending) {
                        Row(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(Color(0xFFFAFAFA))
                                .border(1.dp, Color(0xFFEBEBEB), RoundedCornerShape(9999.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF10B981), CircleShape)
                            )
                            Text(
                                "ĐANG GỬI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color(0xFF171717)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFAFAFA))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: 2x2 Developer Metric Tiles
            OverviewMetricsGrid(
                totalCustomers = dashboardStats.totalCustomers,
                readyCount = dashboardStats.sim1ReadyCount + dashboardStats.sim2ReadyCount,
                sim1Assigned = dashboardStats.sim1AssignedCount,
                sim2Assigned = dashboardStats.sim2AssignedCount
            )

            // Section 2: SIM Status Cards
            Text(
                text = "TRẠNG THÁI SIM",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF888888)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernSimCard(
                    simSlot = 1,
                    config = sim1Config,
                    assignedCount = dashboardStats.sim1AssignedCount,
                    readyCount = dashboardStats.sim1ReadyCount,
                    onClick = { onSelectSim(1) },
                    modifier = Modifier.weight(1f)
                )
                ModernSimCard(
                    simSlot = 2,
                    config = sim2Config,
                    assignedCount = dashboardStats.sim2AssignedCount,
                    readyCount = dashboardStats.sim2ReadyCount,
                    onClick = { onSelectSim(2) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Section 3: 3-Step Guided Workflow
            Text(
                text = "QUY TRÌNH THỰC HIỆN",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF888888)
            )

            // Step 1: Customer & Excel
            WorkflowStepCard(
                stepTag = "01",
                title = "Nhập danh sách khách hàng",
                description = "Nạp file Excel (.xlsx, .xls) hoặc quản lý liên hệ và phân bổ SIM.",
                primaryButtonText = "Nhập Excel",
                onPrimaryClick = {
                    filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                },
                secondaryButtonText = "Xem danh sách",
                onSecondaryClick = onNavigateToCustomers,
                extraAction = {
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        enabled = !isSending,
                        shape = RoundedCornerShape(9999.dp),
                        border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF888888)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Đặt lại trạng thái gửi", fontSize = 13.sp)
                    }
                }
            )

            // Step 2: Message Template
            WorkflowStepCard(
                stepTag = "02",
                title = "Soạn thảo mẫu tin & Spintax",
                description = "Cá nhân hóa nội dung với các biến {Tên_KH}, {Số_tiền} và trộn văn bản chống chặn.",
                primaryButtonText = "Quản lý mẫu tin",
                onPrimaryClick = { onNavigateToManagement(0) }
            )

            // Step 3: Sending & Progress
            WorkflowStepCard(
                stepTag = "03",
                title = "Khởi chạy & Giám sát tiến độ",
                description = "Bắt đầu gửi tin tự động theo hạn mức và độ trễ ngẫu nhiên an toàn.",
                primaryButtonText = "Mở trung tâm gửi tin",
                onPrimaryClick = onNavigateToProgress,
                highlight = isSending
            )

            // Step 4: Zalo Automation
            WorkflowStepCard(
                stepTag = "04",
                title = "Gửi tin nhắn Zalo tự động",
                description = "Tự động hóa tìm SĐT, kết bạn và gửi tin nhắn/hình ảnh qua ứng dụng Zalo bằng Trợ năng.",
                primaryButtonText = "Mở Zalo Auto",
                onPrimaryClick = onNavigateToAutoZalo
            )
        }
    }

    pendingImportState?.let { state ->
        SelectPhoneColumnDialog(
            headers = state.headers,
            selectedHeader = state.selectedHeader,
            onSelectHeader = { customerViewModel.updateSelectedImportHeader(it) },
            selectedImportMode = state.selectedImportMode,
            onSelectImportMode = { customerViewModel.updateSelectedImportMode(it) },
            onConfirm = {
                customerViewModel.confirmImport { result ->
                    result.onSuccess { count ->
                        Toast.makeText(context, "Đã nhập $count khách hàng", Toast.LENGTH_SHORT)
                            .show()
                    }.onFailure { err ->
                        Toast.makeText(
                            context,
                            "Lỗi đọc file Excel: ${err.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            onDismiss = { customerViewModel.cancelImport() }
        )
    }

    if (showResetDialog) {
        ResetStatusDialog(
            onConfirm = { scope ->
                customerViewModel.resetCustomerStatus(scope) {
                    Toast.makeText(
                        context,
                        "Đã đặt lại trạng thái về 'Chờ gửi'",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }

    if (isImporting) {
        LoadingDialog(message = importLoadingMessage)
    }
}
