package com.gmail.nthai.presentation.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.presentation.components.common.LogRecordCard
import com.gmail.nthai.presentation.components.progress.SimSendingProgressCard
import com.gmail.nthai.presentation.viewmodel.ProgressViewModel
import com.gmail.nthai.presentation.viewmodel.ReportViewModel
import com.gmail.nthai.service.SendingServiceState
import com.gmail.nthai.util.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendingProgressScreen(
    showTopBar: Boolean = true,
    viewModel: ProgressViewModel = hiltViewModel(),
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val serviceStates by viewModel.serviceStates.collectAsStateWithLifecycle()
    val plannedMessageCounts by viewModel.plannedMessageCounts.collectAsStateWithLifecycle()
    val simConfigs by viewModel.simConfigs.collectAsStateWithLifecycle()
    val logs by reportViewModel.logs.collectAsStateWithLifecycle()
    var permissionSimSlot by remember { mutableIntStateOf(1) }
    var showPermissionSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        val granted = PermissionUtils.hasRequiredPermissions(context)
        if (granted) {
            viewModel.startSending(context, permissionSimSlot)
        } else {
            showPermissionSettingsDialog = true
        }
    }

    if (showPermissionSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionSettingsDialog = false },
            containerColor = Color(0xFFFFFFFF),
            shape = RoundedCornerShape(16.dp),
            title = { Text("Cần cấp quyền gửi SMS", fontWeight = FontWeight.Bold, color = Color(0xFF171717)) },
            text = {
                Text(
                    "Ứng dụng cần quyền Điện thoại, SMS và Thông báo để tự động gửi tin nhắn trong nền. " +
                        "Hãy bấm 'Mở cài đặt' để kích hoạt các quyền này.",
                    color = Color(0xFF4D4D4D)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionSettingsDialog = false
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                        )
                    }
                ) {
                    Text("Mở cài đặt", fontWeight = FontWeight.Bold, color = Color(0xFF171717))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionSettingsDialog = false }) {
                    Text("Để sau", color = Color(0xFF888888))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            "Tiến độ gửi tin",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF171717)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFFFFFF)
                    )
                )
            }
        }
    ) { innerPadding ->
        val activeSimCount = serviceStates.values.count {
            it is SendingServiceState.Running || it is SendingServiceState.Paused
        }
        val plannedCount = plannedMessageCounts.values.sum()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFAFAFA))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFFFFF),
                border = BorderStroke(1.dp, Color(0xFFEBEBEB))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "ĐIỀU PHỐI SMS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF888888)
                        )
                        Text(
                            text = if (activeSimCount > 0) "$activeSimCount SIM đang gửi tin..."
                            else "Hệ thống an toàn với độ trễ ngẫu nhiên",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            plannedCount.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF171717)
                        )
                        Text(
                            "TIN CẦN GỬI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp
                            ),
                            color = Color(0xFF888888)
                        )
                    }
                }
            }

            // SIM Progress Cards
            (1..2).forEach { simSlot ->
                val carrierName = simConfigs.find { it.simSlot == simSlot }?.carrierName?.trim().orEmpty()
                SimSendingProgressCard(
                    simSlot = simSlot,
                    carrierName = carrierName,
                    state = serviceStates[simSlot] ?: SendingServiceState.Idle,
                    plannedCount = plannedMessageCounts[simSlot] ?: 0,
                    onStart = {
                        if (PermissionUtils.hasRequiredPermissions(context)) {
                            viewModel.startSending(context, simSlot)
                        } else {
                            permissionSimSlot = simSlot
                            permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                        }
                    },
                    onPause = { viewModel.pauseSending(context, simSlot) },
                    onResume = { viewModel.resumeSending(context, simSlot) },
                    onStop = { viewModel.stopSending(context, simSlot) }
                )
            }

            // Recent Logs Section
            val recentLogs = remember(logs) { logs.take(5) }
            if (recentLogs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NHẬT KÝ GỬI GẦN NHẤT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF888888)
                    )
                    Text(
                        text = "${recentLogs.size}/${logs.size} tin",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp
                        ),
                        color = Color(0xFF888888)
                    )
                }

                recentLogs.forEach { log ->
                    LogRecordCard(log = log, compact = true)
                }
            }
        }
    }
}
