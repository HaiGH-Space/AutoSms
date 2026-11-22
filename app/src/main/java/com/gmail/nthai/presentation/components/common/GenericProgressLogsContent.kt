package com.gmail.nthai.presentation.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmail.nthai.domain.model.LogRecord

data class CampaignProgressData(
    val title: String = "TIẾN TRÌNH CHIẾN DỊCH",
    val statusText: String = "Sẵn sàng",
    val processedCount: Int = 0,
    val totalCount: Int = 0,
    val currentTargetInfo: String? = null,
    val countdownSeconds: Int? = null,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false
)

@Composable
fun GenericProgressLogsContent(
    progressData: CampaignProgressData,
    logs: List<LogRecord>,
    modifier: Modifier = Modifier,
    onPauseCampaign: (() -> Unit)? = null,
    onResumeCampaign: (() -> Unit)? = null,
    onStopCampaign: (() -> Unit)? = null,
    onClearLogs: (() -> Unit)? = null,
    emptyLogMessage: String = "Chưa có nhật ký gửi tin nào.",
    configSection: (@Composable () -> Unit)? = null
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Optional Config Section Slot (e.g. Call Config / SIM Slot)
        configSection?.invoke()

        // 1. Progress Summary Card
        AutoSmsCardContainer(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = progressData.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF888888)
                        )
                        Text(
                            text = progressData.statusText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF171717)
                        )
                    }

                    Text(
                        text = "${progressData.processedCount} / ${if (progressData.totalCount > 0) progressData.totalCount else 0}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF171717)
                    )
                }

                val progressRatio = if (progressData.totalCount > 0) {
                    (progressData.processedCount.toFloat() / progressData.totalCount).coerceIn(0f, 1f)
                } else 0f

                LinearProgressIndicator(
                    progress = { progressRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF171717),
                    trackColor = Color(0xFFEBEBEB)
                )

                if (!progressData.currentTargetInfo.isNullOrEmpty()) {
                    Text(
                        text = "Đang xử lý: ${progressData.currentTargetInfo}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF555555)
                    )
                }

                if (progressData.countdownSeconds != null && progressData.countdownSeconds > 0) {
                    Text(
                        text = "Đếm ngược chờ gửi: ${progressData.countdownSeconds}s",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0070F3)
                    )
                }

                // Action Controls Row (Pause / Resume / Stop)
                if (progressData.isRunning || progressData.isPaused) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (progressData.isPaused) {
                            if (onResumeCampaign != null) {
                                Button(
                                    onClick = onResumeCampaign,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF171717)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Tiếp tục")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tiếp tục")
                                }
                            }
                        } else {
                            if (onPauseCampaign != null) {
                                OutlinedButton(
                                    onClick = onPauseCampaign,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = "Tạm dừng")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tạm dừng", color = Color(0xFF171717))
                                }
                            }
                        }

                        if (onStopCampaign != null) {
                            OutlinedButton(
                                onClick = onStopCampaign,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEE0000)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Dừng chiến dịch")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Dừng", color = Color(0xFFEE0000))
                            }
                        }
                    }
                }
            }
        }

        // 2. Logs Header & Clear Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LỊCH SỬ NHẬT KÝ CHIẾN DỊCH (${logs.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF888888)
            )

            if (onClearLogs != null && logs.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa nhật ký",
                        tint = Color(0xFF888888)
                    )
                }
            }
        }

        // 3. Logs List / Empty State
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emptyLogMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF888888)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = logs,
                    key = { it.id }
                ) { logRecord ->
                    LogRecordCard(log = logRecord)
                }
            }
        }
    }

    if (showClearDialog && onClearLogs != null) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Xác nhận xóa nhật ký", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa tất cả lịch sử nhật ký không?") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearLogs()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEE0000)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Xóa", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hủy", color = Color(0xFF171717))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}
