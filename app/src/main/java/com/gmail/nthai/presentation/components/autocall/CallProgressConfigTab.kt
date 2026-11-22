package com.gmail.nthai.presentation.components.autocall

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.gmail.nthai.domain.model.AutoCallProgress
import com.gmail.nthai.domain.model.AutoCallStatus
import com.gmail.nthai.domain.model.CallLogRecord
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.model.SimConfig
import com.gmail.nthai.presentation.components.common.CampaignProgressData
import com.gmail.nthai.presentation.components.common.GenericProgressLogsContent

@Composable
fun CallProgressConfigTab(
    context: Context,
    hasCallPermission: Boolean,
    onHasCallPermissionChange: (Boolean) -> Unit,
    permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>,
    delaySeconds: Int,
    onDelayChange: (Int) -> Unit,
    selectedSimSlot: Int,
    onSimSlotChange: (Int) -> Unit,
    simConfigs: List<SimConfig>,
    totalRecipientsCount: Int,
    selectedRecipientsCount: Int,
    progressState: AutoCallProgress,
    callLogs: List<CallLogRecord>,
    onStartCampaign: () -> Unit,
    onPauseCampaign: () -> Unit,
    onResumeCampaign: () -> Unit,
    onStopCampaign: () -> Unit,
    onClearCallLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRunning = progressState.status == AutoCallStatus.RUNNING
    val isPaused = progressState.status == AutoCallStatus.PAUSED
    val statusText = when (progressState.status) {
        AutoCallStatus.RUNNING -> if (progressState.isWaitingDelay) "Đang chờ đếm ngược..." else "Đang thực hiện cuộc gọi..."
        AutoCallStatus.PAUSED -> "Tạm dừng"
        AutoCallStatus.FINISHED -> "Đã hoàn thành"
        AutoCallStatus.CANCELLED -> "Đã hủy"
        AutoCallStatus.IDLE -> "Sẵn sàng"
    }

    val progressData = CampaignProgressData(
        title = "TIẾN TRÌNH GỌI TỰ ĐỘNG",
        statusText = statusText,
        processedCount = progressState.completedCount,
        totalCount = progressState.totalCount,
        currentTargetInfo = progressState.currentCustomerName ?: progressState.currentPhone,
        isRunning = isRunning,
        isPaused = isPaused
    )

    val logs = callLogs.map { log ->
        LogRecord(
            id = log.id,
            customerId = log.customerId,
            customerName = log.customerName,
            phone = log.phone,
            messageContent = if (log.callDurationSeconds > 0) "Cuộc gọi (${log.callDurationSeconds}s)" else "Cuộc gọi tự động",
            simSlot = selectedSimSlot,
            timestamp = log.timestamp,
            status = if (log.status == "SUCCESS" || log.status == "CONNECTED") LogStatus.SENT_SUCCESS else LogStatus.FAILED,
            errorMessage = log.errorMessage
        )
    }

    GenericProgressLogsContent(
        progressData = progressData,
        logs = logs,
        modifier = modifier.fillMaxSize(),
        onPauseCampaign = onPauseCampaign,
        onResumeCampaign = onResumeCampaign,
        onStopCampaign = onStopCampaign,
        onClearLogs = onClearCallLogs,
        emptyLogMessage = "Chưa có nhật ký cuộc gọi nào.",
        configSection = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (!hasCallPermission) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFEFCF),
                        border = BorderStroke(1.dp, Color(0xFFF5A623))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFF5A623)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Chưa cấp quyền gọi điện",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF171717)
                                    )
                                    Text(
                                        text = "Cần quyền CALL_PHONE và READ_PHONE_STATE để tự động gọi.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF4D4D4D)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val callGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                                    val stateGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                                    if (callGranted && stateGranted) {
                                        onHasCallPermissionChange(true)
                                    } else {
                                        try {
                                            permissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.CALL_PHONE,
                                                    Manifest.permission.READ_PHONE_STATE
                                                )
                                            )
                                        } catch (_: Exception) {
                                            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = android.net.Uri.fromParts("package", context.packageName, null)
                                            }
                                            context.startActivity(intent)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF171717))
                            ) {
                                Text("Cấp quyền", color = Color(0xFFFFFFFF))
                            }
                        }
                    }
                }

                AutoCallConfigCard(
                    delaySeconds = delaySeconds,
                    onDelayChange = onDelayChange,
                    selectedSimSlot = selectedSimSlot,
                    onSimSlotChange = onSimSlotChange,
                    simConfigs = simConfigs,
                    totalCustomersCount = totalRecipientsCount,
                    selectedCustomersCount = selectedRecipientsCount,
                    isRunning = isRunning || isPaused,
                    onStartClick = {
                        if (totalRecipientsCount == 0) {
                            android.widget.Toast.makeText(context, "Vui lòng thêm danh bạ gọi trước khi bắt đầu", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (hasCallPermission) {
                            onStartCampaign()
                        } else {
                            try {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.CALL_PHONE,
                                        Manifest.permission.READ_PHONE_STATE
                                    )
                                )
                            } catch (_: Exception) {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = android.net.Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        }
                    }
                )
            }
        }
    )
}
