package com.gmail.nthai.presentation.components.progress

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
        is SendingServiceState.Running -> if (failedCount > 0) "Đang gửi · $failedCount lỗi" else "Đang gửi"
        is SendingServiceState.Paused -> if (failedCount > 0) "Tạm dừng · $failedCount lỗi" else "Tạm dừng"
        is SendingServiceState.Finished -> if (failedCount > 0) "Hoàn tất · $failedCount lỗi" else "Hoàn tất"
        is SendingServiceState.Error -> "Có lỗi"
    }
    val statusColor = when (state) {
        is SendingServiceState.Running -> if (failedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        is SendingServiceState.Paused -> if (failedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
        is SendingServiceState.Finished -> if (failedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        is SendingServiceState.Error -> MaterialTheme.colorScheme.error
        SendingServiceState.Idle -> MaterialTheme.colorScheme.onSurfaceVariant
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
            title = { Text("Dừng gửi bằng SIM $simSlot?") },
            text = {
                Text("Tiến trình hiện tại sẽ kết thúc. Các tin chưa gửi vẫn có thể được gửi sau.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStopConfirmation = false
                        onStop()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Dừng gửi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirmation = false }) {
                    Text("Tiếp tục")
                }
            }
        )
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
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
                        text = simHeader,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when (state) {
                            is SendingServiceState.Running -> "Tiến trình gửi tin"
                            is SendingServiceState.Paused -> "Có thể tiếp tục bất cứ lúc nào"
                            is SendingServiceState.Finished -> "Đã xử lý xong danh sách"
                            is SendingServiceState.Error -> state.message
                            SendingServiceState.Idle -> if (plannedCount > 0) {
                                "$plannedCount tin sẵn sàng gửi"
                            } else {
                                "Chưa có tin nhắn trong hàng đợi"
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state is SendingServiceState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (state is SendingServiceState.Error) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                SendingStatusBadge(
                    text = statusText,
                    color = statusColor,
                    isPulsing = state is SendingServiceState.Running
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(64.dp),
                        strokeWidth = 6.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        color = statusColor
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Tin đã xử lý",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$sent / $total",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (failedCount > 0) "$failedCount tin gửi lỗi"
                        else "${(total - sent).coerceAtLeast(0)} tin còn lại",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (failedCount > 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (failedCount > 0) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (state is SendingServiceState.Running) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = if (state.secondsUntilNext != null) "Vừa gửi tới" else "Đang gửi tới",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.currentCustomerName.ifBlank { "Đang chuẩn bị tin nhắn..." },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    state.secondsUntilNext?.let { seconds ->
                        Text(
                            text = "Tin tiếp theo sau $seconds giây",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

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
                                .defaultMinSize(minHeight = 44.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = if (state is SendingServiceState.Error) "Thử lại SIM $simSlot"
                                else "Gửi bằng SIM $simSlot"
                            )
                        }
                    }
                    is SendingServiceState.Running -> {
                        Button(
                            onClick = onPause,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Tạm dừng")
                        }
                        Button(
                            onClick = { showStopConfirmation = true },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Dừng gửi")
                        }
                    }
                    is SendingServiceState.Paused -> {
                        Button(
                            onClick = onResume,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Tiếp tục")
                        }
                        Button(
                            onClick = { showStopConfirmation = true },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Dừng gửi")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SendingStatusBadge(
    text: String,
    color: Color,
    isPulsing: Boolean
) {
    val transition = rememberInfiniteTransition(label = "sending status pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sending status alpha"
    )
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(if (isPulsing) pulseAlpha else 1f)
                    .background(color, CircleShape)
            )
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
