package com.gmail.nthai.presentation.components.progress

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmail.nthai.service.SendingServiceState

@Composable
fun SimSendingProgressCard(
    simSlot: Int,
    carrierName: String = "",
    state: SendingServiceState,
    plannedCount: Int,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (sent, total) = when (state) {
        is SendingServiceState.Running -> state.sentCount to state.totalCount
        is SendingServiceState.Paused -> state.sentCount to state.totalCount
        is SendingServiceState.Finished -> state.sentCount to state.totalCount
        SendingServiceState.Idle -> 0 to plannedCount
        is SendingServiceState.Error -> state.sentCount to state.totalCount
    }
    val failedCount = when (state) {
        is SendingServiceState.Running -> state.failedCount
        is SendingServiceState.Paused -> state.failedCount
        is SendingServiceState.Finished -> state.failedCount
        is SendingServiceState.Error -> state.failedCount
        SendingServiceState.Idle -> 0
    }
    val statusText = when (state) {
        SendingServiceState.Idle -> if (plannedCount > 0) "Sẵn sàng" else "Chưa có liên hệ"
        is SendingServiceState.Running -> if (failedCount > 0) "Đang gửi ($failedCount lỗi)" else "Đang gửi"
        is SendingServiceState.Paused -> if (failedCount > 0) "Tạm dừng ($failedCount lỗi)" else "Tạm dừng"
        is SendingServiceState.Finished -> if (failedCount > 0) "Hoàn tất ($failedCount lỗi)" else "Hoàn tất"
        is SendingServiceState.Error -> "Có lỗi"
    }
    val dotColor = when (state) {
        is SendingServiceState.Running -> if (failedCount > 0) Color(0xFFEE0000) else Color(
            0xFF10B981
        )

        is SendingServiceState.Paused -> if (failedCount > 0) Color(0xFFEE0000) else Color(
            0xFFF5A623
        )

        is SendingServiceState.Finished -> if (failedCount > 0) Color(0xFFEE0000) else Color(
            0xFF0070F3
        )

        is SendingServiceState.Error -> Color(0xFFEE0000)
        SendingServiceState.Idle -> Color(0xFF888888)
    }
    val simHeader = if (carrierName.isNotBlank()) "SIM $simSlot ($carrierName)" else "SIM $simSlot"
    val progress = if (total > 0) (sent.toFloat() / total).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "SIM $simSlot progress"
    )
    var showStopConfirmation by remember(simSlot) { mutableStateOf(false) }

    if (showStopConfirmation) {
        AlertDialog(
            onDismissRequest = { showStopConfirmation = false },
            containerColor = Color(0xFFFFFFFF),
            shape = RoundedCornerShape(12.dp),
            title = {
                Text(
                    "Dừng gửi bằng SIM $simSlot?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717)
                )
            },
            text = {
                Text(
                    "Tiến trình gửi tin sẽ kết thúc ngay lập tức. Các số điện thoại chưa gửi vẫn được lưu trạng thái để gửi sau.",
                    color = Color(0xFF4D4D4D)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStopConfirmation = false
                        onStop()
                    }
                ) {
                    Text("Dừng gửi", fontWeight = FontWeight.Bold, color = Color(0xFFEE0000))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirmation = false }) {
                    Text("Tiếp tục gửi", color = Color(0xFF171717))
                }
            }
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, Color(0xFFEBEBEB))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF5F5F5),
                        border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.SimCard,
                                contentDescription = null,
                                tint = Color(0xFF171717),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = simHeader,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF171717),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (state) {
                                is SendingServiceState.Running -> "Tiến trình đang hoạt động"
                                is SendingServiceState.Paused -> "Tạm dừng · Sẵn sàng tiếp tục"
                                is SendingServiceState.Finished -> "Đã hoàn thành toàn bộ danh sách"
                                is SendingServiceState.Error -> state.message
                                SendingServiceState.Idle -> if (plannedCount > 0) "$plannedCount tin trong hàng đợi" else "Chưa có liên hệ chờ gửi"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state is SendingServiceState.Error) Color(0xFFEE0000) else Color(
                                0xFF888888
                            )
                        )
                    }
                }
                SendingStatusBadge(
                    text = statusText,
                    dotColor = dotColor,
                    isPulsing = state is SendingServiceState.Running
                )
            }

            // Progress & Counters Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(64.dp),
                        strokeWidth = 5.dp,
                        trackColor = Color(0xFFEBEBEB),
                        color = Color(0xFF171717)
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF171717)
                    )
                }

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ProgressStatBox(
                        label = "ĐÃ GỬI",
                        value = sent.toString(),
                        color = Color(0xFF171717)
                    )
                    ProgressStatBox(
                        label = "LỖI",
                        value = failedCount.toString(),
                        color = if (failedCount > 0) Color(0xFFEE0000) else Color(0xFF888888)
                    )
                    ProgressStatBox(
                        label = "CÒN LẠI",
                        value = (total - sent).coerceAtLeast(0).toString(),
                        color = Color(0xFF888888)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(9999.dp)),
                color = Color(0xFF171717),
                trackColor = Color(0xFFEBEBEB)
            )

            // Current Sending Detail / Countdown
            if (state is SendingServiceState.Running) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFAFAFA),
                    border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (state.secondsUntilNext != null) "Vừa gửi tin nhắn thành công:" else "Đang chuẩn bị gửi:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF888888)
                        )
                        Text(
                            text = state.currentCustomerName.ifBlank { "Đang đọc thông tin người nhận..." },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF171717),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        state.secondsUntilNext?.let { seconds ->
                            Text(
                                text = "⏱ Giãn cách: tin tiếp theo sau ${seconds}s",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF171717)
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (state) {
                    SendingServiceState.Idle, is SendingServiceState.Finished, is SendingServiceState.Error -> {
                        Button(
                            onClick = onStart,
                            enabled = plannedCount > 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF171717),
                                contentColor = Color(0xFFFFFFFF),
                                disabledContainerColor = Color(0xFFEBEBEB),
                                disabledContentColor = Color(0xFF888888)
                            )
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state is SendingServiceState.Error) "Thử lại SIM $simSlot"
                                else "Bắt đầu gửi (SIM $simSlot)",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    is SendingServiceState.Running -> {
                        OutlinedButton(
                            onClick = onPause,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF171717)
                            )
                        ) {
                            Icon(
                                Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tạm dừng", fontWeight = FontWeight.Medium)
                        }
                        OutlinedButton(
                            onClick = { showStopConfirmation = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFF7D4D6)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEE0000)
                            )
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dừng gửi", fontWeight = FontWeight.Medium)
                        }
                    }

                    is SendingServiceState.Paused -> {
                        Button(
                            onClick = onResume,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF171717),
                                contentColor = Color(0xFFFFFFFF)
                            )
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tiếp tục", fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = { showStopConfirmation = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFF7D4D6)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEE0000)
                            )
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dừng gửi", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressStatBox(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp
            ),
            color = Color(0xFF888888)
        )
    }
}

@Composable
private fun SendingStatusBadge(
    text: String,
    dotColor: Color,
    isPulsing: Boolean
) {
    val transition = rememberInfiniteTransition(label = "sending status pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sending status alpha"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(Color(0xFFFAFAFA))
            .border(1.dp, Color(0xFFEBEBEB), RoundedCornerShape(9999.dp))
            .padding(horizontal = 9.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .alpha(if (isPulsing) pulseAlpha else 1f)
                .background(dotColor, CircleShape)
        )
        Text(
            text = text,
            color = Color(0xFF171717),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}
