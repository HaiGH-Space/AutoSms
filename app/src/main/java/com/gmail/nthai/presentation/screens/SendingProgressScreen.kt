package com.gmail.nthai.presentation.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.presentation.viewmodel.ProgressViewModel
import com.gmail.nthai.service.SendingServiceState
import com.gmail.nthai.util.PermissionUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            (1..2).forEach { simSlot ->
                val carrierName = simConfigs.find { it.simSlot == simSlot }?.carrierName?.trim().orEmpty()
                SimSendingCard(
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
private fun SimSendingCard(
    simSlot: Int,
    carrierName: String = "",
    state: SendingServiceState,
    plannedCount: Int,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    val (sent, total) = when (state) {
        is SendingServiceState.Running -> state.sentCount to state.totalCount
        is SendingServiceState.Paused -> state.sentCount to state.totalCount
        is SendingServiceState.Finished -> state.sentCount to state.totalCount
        SendingServiceState.Idle -> 0 to plannedCount
        is SendingServiceState.Error -> state.sentCount to state.totalCount
    }
    val title = when (state) {
        SendingServiceState.Idle -> if (plannedCount > 0) "Sẵn sàng gửi $plannedCount tin" else "Chưa có số được chọn"
        is SendingServiceState.Running -> "Đang gửi: ${state.currentCustomerName}"
        is SendingServiceState.Paused -> "Đã tạm dừng"
        is SendingServiceState.Finished -> "Hoàn thành"
        is SendingServiceState.Error -> "Lỗi: ${state.message}"
    }

    val simHeader = if (carrierName.isNotBlank()) "SIM $simSlot ($carrierName)" else "SIM $simSlot"
    val buttonText = "Gửi bằng SIM $simSlot"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(simHeader, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(
                progress = { if (total > 0) sent.toFloat() / total else 0f },
                modifier = Modifier.fillMaxWidth().height(10.dp)
            )
            Text("Tiến độ: $sent / $total tin nhắn")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                when (state) {
                    SendingServiceState.Idle, is SendingServiceState.Finished, is SendingServiceState.Error -> {
                        Button(onClick = onStart, enabled = plannedCount > 0) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(buttonText)
                        }
                    }
                    is SendingServiceState.Running -> {
                        OutlinedButton(onClick = onPause) {
                            Icon(Icons.Default.Pause, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Tạm dừng")
                        }
                        Button(
                            onClick = onStop,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Dừng")
                        }
                    }
                    is SendingServiceState.Paused -> {
                        Button(onClick = onResume) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Tiếp tục")
                        }
                        Button(
                            onClick = onStop,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Dừng")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogItemCard(log: LogRecord) {
    val dateFormat = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault())

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${log.customerName} (${log.phone})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(Date(log.timestamp)),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.messageContent,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SIM ${log.simSlot}",
                    style = MaterialTheme.typography.labelSmall
                )

                val (text, color) = when (log.status) {
                    LogStatus.SENT_SUCCESS -> Pair("Thành công", MaterialTheme.colorScheme.primary)
                    LogStatus.FAILED -> Pair("Lỗi: ${log.errorMessage ?: ""}", MaterialTheme.colorScheme.error)
                    LogStatus.PENDING -> Pair("Đang xử lý...", MaterialTheme.colorScheme.secondary)
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
