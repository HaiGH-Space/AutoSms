package com.gmail.nthai.presentation.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.presentation.components.common.LogRecordCard
import com.gmail.nthai.presentation.components.progress.SimSendingProgressCard
import com.gmail.nthai.presentation.viewmodel.ProgressViewModel
import com.gmail.nthai.service.SendingServiceState
import com.gmail.nthai.util.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendingProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val serviceStates by viewModel.serviceStates.collectAsStateWithLifecycle()
    val plannedMessageCounts by viewModel.plannedMessageCounts.collectAsStateWithLifecycle()
    val simConfigs by viewModel.simConfigs.collectAsStateWithLifecycle()
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
            title = { Text("Cần cấp quyền gửi SMS") },
            text = {
                Text(
                    "Android không cho phép ứng dụng tự cấp quyền vĩnh viễn. " +
                        "Hãy mở Cài đặt ứng dụng và bật quyền Điện thoại, SMS và Thông báo " +
                        "để không phải xác nhận lại khi gửi."
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
                    Text("Mở cài đặt")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionSettingsDialog = false }) {
                    Text("Để sau")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tiến độ gửi SMS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Trung tâm gửi tin",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            if (activeSimCount > 0) "$activeSimCount SIM đang hoạt động"
                            else "Theo dõi và điều khiển từng SIM",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            plannedCount.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "tin cần gửi",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

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
        }
    }
}

@Composable
fun LogItemCard(log: LogRecord) {
    LogRecordCard(log = log, compact = true)
}
