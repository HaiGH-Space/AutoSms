package com.gmail.nthai.presentation.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    val serviceState by viewModel.serviceState.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.startSending(context)
        } else {
            Toast.makeText(context, "Cần cấp đầy đủ quyền SIM & SMS để gửi tin nhắn!", Toast.LENGTH_LONG).show()
        }
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val statusTitle = when (val state = serviceState) {
                        SendingServiceState.Idle -> "Đang rảnh (Sẵn sàng gửi)"
                        is SendingServiceState.Running -> "Đang gửi: ${state.currentCustomerName}"
                        is SendingServiceState.Paused -> "Đã tạm dừng"
                        is SendingServiceState.Finished -> "Hoàn thành gửi tin"
                        is SendingServiceState.Error -> "Tạm dừng do lỗi: ${state.message}"
                    }

                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val (sent, total) = when (val state = serviceState) {
                        is SendingServiceState.Running -> Pair(state.sentCount, state.totalCount)
                        is SendingServiceState.Paused -> Pair(state.sentCount, state.totalCount)
                        is SendingServiceState.Finished -> Pair(state.sentCount, state.totalCount)
                        else -> Pair(0, 0)
                    }

                    val progressFraction = if (total > 0) sent.toFloat() / total else 0f

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                    )

                    Text(
                        text = "Tiến độ: $sent / $total tin nhắn",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (serviceState) {
                            SendingServiceState.Idle, is SendingServiceState.Finished, is SendingServiceState.Error -> {
                                Button(
                                    onClick = {
                                        if (PermissionUtils.hasRequiredPermissions(context)) {
                                            viewModel.startSending(context)
                                        } else {
                                            permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    Text("Bắt đầu gửi")
                                }
                            }

                            is SendingServiceState.Running -> {
                                OutlinedButton(
                                    onClick = { viewModel.pauseSending(context) }
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null)
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    Text("Tạm dừng")
                                }

                                Button(
                                    onClick = { viewModel.stopSending(context) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null)
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    Text("Dừng hẳn")
                                }
                            }

                            is SendingServiceState.Paused -> {
                                Button(
                                    onClick = { viewModel.resumeSending(context) }
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    Text("Tiếp tục")
                                }

                                Button(
                                    onClick = { viewModel.stopSending(context) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null)
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    Text("Dừng hẳn")
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "Nhật ký gửi thời gian thực:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có nhật ký phát tin.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        LogItemCard(log)
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
        modifier = Modifier.fillMaxWidth()
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
