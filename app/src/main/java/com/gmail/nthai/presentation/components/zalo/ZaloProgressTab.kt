package com.gmail.nthai.presentation.components.zalo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.presentation.components.common.AutoSmsCardContainer
import com.gmail.nthai.presentation.components.common.LogRecordCard
import com.gmail.nthai.service.ZaloSendingProgressState

@Composable
fun ZaloProgressTab(
    progressState: ZaloSendingProgressState = ZaloSendingProgressState(),
    logs: List<LogRecord> = emptyList(),
    onPauseCampaign: () -> Unit = {},
    onResumeCampaign: () -> Unit = {},
    onStopCampaign: () -> Unit = {},
    onClearLogs: () -> Unit = {}
) {
    val isSending = progressState.isSending
    val isPaused = progressState.isPaused
    val sentCount = progressState.sentCount
    val totalCount = if (progressState.totalCount > 0) progressState.totalCount else 1
    val currentRecipient = progressState.currentRecipient
    val countdownSeconds = progressState.countdownSeconds
    var showClearConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Zalo Progress Center Card
        AutoSmsCardContainer(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "TRUNG TÂM GỬI ZALO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF888888)
                    )
                    Text(
                        text = when {
                            !isSending -> progressState.statusMessage
                            isPaused -> "Trạng thái: Tạm dừng"
                            else -> progressState.statusMessage
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF171717),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$sentCount / $totalCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF171717)
                    )
                    Text(
                        text = "tin Zalo",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF888888)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { sentCount.toFloat() / totalCount },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(9999.dp)),
                color = Color(0xFF171717),
                trackColor = Color(0xFFEBEBEB)
            )

            if (isSending && currentRecipient != null) {
                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF5F5F5),
                    border = BorderStroke(1.dp, Color(0xFFEBEBEB))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Đang xử lý: ${currentRecipient.name} (${currentRecipient.phone})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF171717)
                            )
                            Text(
                                text = "Đang đếm ngược giãn cách chống spam Zalo...",
                                fontSize = 11.sp,
                                color = Color(0xFF666666)
                            )
                        }
                        if (countdownSeconds > 0) {
                            Text(
                                text = "${countdownSeconds}s",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF171717)
                            )
                        }
                    }
                }
            }

            if (isSending) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isPaused) onResumeCampaign() else onPauseCampaign()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(9999.dp),
                        border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF171717))
                    ) {
                        Icon(
                            if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isPaused) "Tiếp tục" else "Tạm dừng",
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = onStopCampaign,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEE0000),
                            contentColor = Color(0xFFFFFFFF)
                        )
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Dừng",
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 2. Real-time Log Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NHẬT KÝ TỰ ĐỘNG GỬI ZALO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF888888)
            )

            if (logs.isNotEmpty()) {
                IconButton(
                    onClick = { showClearConfirmation = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFEE2E2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa nhật ký Zalo",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFEE0000)
                        )
                    }
                }
            }
        }

        // 3. Log List (Using App's standard LogRecordCard)
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chưa có nhật ký gửi Zalo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF888888)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    LogRecordCard(log = log, compact = true)
                }
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color(0xFFFFFFFF),
            title = {
                Text(
                    "Xóa nhật ký Zalo?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717)
                )
            },
            text = {
                Text(
                    "Thao tác này sẽ xóa ${logs.size} bản ghi nhật ký gửi Zalo khỏi thiết bị và không thể hoàn tác.",
                    color = Color(0xFF4D4D4D)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearLogs()
                        showClearConfirmation = false
                    },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEE0000),
                        contentColor = Color(0xFFFFFFFF)
                    )
                ) {
                    Text("Xóa nhật ký", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearConfirmation = false },
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF171717)
                    )
                ) {
                    Text("Hủy", fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ZaloProgressTabPreview() {
    MaterialTheme {
        ZaloProgressTab(
            logs = listOf(
                LogRecord(
                    id = 1,
                    customerId = 101,
                    customerName = "Nguyễn Văn Anh",
                    phone = "0987654321",
                    messageContent = "Chào bạn, bên em có ưu đãi mã giảm giá.",
                    simSlot = 0,
                    status = LogStatus.SENT_SUCCESS,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 12
                )
            )
        )
    }
}
