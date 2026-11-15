package com.gmail.nthai.presentation.components.zalo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFFFFF),
            border = BorderStroke(1.dp, Color(0xFFEBEBEB))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                            color = Color(0xFF171717)
                        )
                    }

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
                            Text(if (isPaused) "Tiếp tục" else "Tạm dừng", fontSize = 13.sp)
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
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dừng chiến dịch", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // 2. Real-time Log Section Header
        Text(
            text = "NHẬT KÝ TỰ ĐỘNG GỬI ZALO",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF888888)
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
