package com.gmail.nthai.presentation.components.zalo

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.presentation.components.common.LogRecordCard
import com.gmail.nthai.service.ZaloSendingProgressState

@Composable
fun ZaloProgressTab(
    progressState: ZaloSendingProgressState = ZaloSendingProgressState(),
    logs: List<LogRecord> = emptyList(),
    onPauseCampaign: () -> Unit = {},
    onResumeCampaign: () -> Unit = {},
    onStopCampaign: () -> Unit = {}
) {
    val isSending = progressState.isSending
    val isPaused = progressState.isPaused
    val sentCount = progressState.sentCount
    val totalCount = if (progressState.totalCount > 0) progressState.totalCount else 1
    val currentRecipient = progressState.currentRecipient
    val countdownSeconds = progressState.countdownSeconds

    val displayLogs = if (logs.isEmpty() && !isSending) {
        listOf(
            LogRecord(
                id = 1,
                customerId = 101,
                customerName = "Nguyễn Văn Anh",
                phone = "0987654321",
                messageContent = "{Xin chào|Chào} Nguyễn Văn Anh, bên em có ưu đãi mã giảm giá 20% cho đơn hàng đầu tiên.",
                simSlot = 0,
                status = LogStatus.SENT_SUCCESS,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 12
            ),
            LogRecord(
                id = 2,
                customerId = 102,
                customerName = "Trần Thị Bình",
                phone = "0912345678",
                messageContent = "Chào bạn, đã tự động bấm gửi lời mời kết bạn & dán tin nhắn trên Zalo.",
                simSlot = 0,
                status = LogStatus.SENT_SUCCESS,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 8
            )
        )
    } else {
        logs
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Zalo Progress Center Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Trung tâm gửi Zalo Trợ năng",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = when {
                                !isSending -> progressState.statusMessage
                                isPaused -> "Trạng thái: Tạm dừng"
                                else -> progressState.statusMessage
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$sentCount / $totalCount",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "tin Zalo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { sentCount.toFloat() / totalCount },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isSending && currentRecipient != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
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
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Đang đếm ngược giãn cách chống spam Zalo...",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (countdownSeconds > 0) {
                                Text(
                                    text = "${countdownSeconds}s",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isSending) {
                        OutlinedButton(
                            onClick = {
                                if (isPaused) onResumeCampaign() else onPauseCampaign()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(
                                if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPaused) "Tiếp tục" else "Tạm dừng")
                        }

                        Button(
                            onClick = onStopCampaign,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dừng chiến dịch")
                        }
                    }
                }
            }
        }

        // 2. Real-time Log Section Header
        Text(
            text = "Nhật ký tự động gửi Zalo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // 3. Log List (Using App's standard LogRecordCard)
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(displayLogs, key = { it.id }) { log ->
                LogRecordCard(log = log, compact = true)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ZaloProgressTabPreview() {
    MaterialTheme {
        ZaloProgressTab()
    }
}
