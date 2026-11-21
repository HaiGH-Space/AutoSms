package com.gmail.nthai.presentation.components.autocall

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmail.nthai.domain.model.AutoCallProgress
import com.gmail.nthai.domain.model.AutoCallStatus
import com.gmail.nthai.presentation.components.common.AutoSmsCardContainer

@Composable
fun AutoCallProgressCard(
    progress: AutoCallProgress,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    if (progress.status == AutoCallStatus.IDLE) return

    AutoSmsCardContainer(title = "Tiến trình gọi tự động") {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (progress.status) {
                        AutoCallStatus.RUNNING -> if (progress.isWaitingDelay) "Đang chờ đếm ngược..." else "Đang thực hiện cuộc gọi..."
                        AutoCallStatus.PAUSED -> "Tạm dừng"
                        AutoCallStatus.FINISHED -> "Đã hoàn thành"
                        AutoCallStatus.CANCELLED -> "Đã hủy"
                        AutoCallStatus.IDLE -> ""
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (progress.status) {
                        AutoCallStatus.RUNNING -> Color(0xFF0070F3)
                        AutoCallStatus.PAUSED -> Color(0xFFF5A623)
                        AutoCallStatus.FINISHED -> Color(0xFF0070F3)
                        else -> Color(0xFFEE0000)
                    }
                )

                Text(
                    text = "${progress.completedCount} / ${progress.totalCount}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progressRatio = if (progress.totalCount > 0)
                progress.completedCount.toFloat() / progress.totalCount
            else 0f

            LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF171717),
                trackColor = Color(0xFFEBEBEB)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!progress.currentCustomerName.isNullOrEmpty() || !progress.currentPhone.isNullOrEmpty()) {
                Text(
                    text = "Khách hàng: ${progress.currentCustomerName ?: progress.currentPhone}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF171717)
                )
                Text(
                    text = "SĐT: ${progress.currentPhone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888)
                )
            }

            AnimatedVisibility(visible = progress.isWaitingDelay) {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tự động gọi số tiếp theo sau: ${progress.countdownRemainingSeconds}s",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF5A623)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (progress.status == AutoCallStatus.RUNNING) {
                    OutlinedButton(
                        onClick = onPause,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF171717))
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, tint = Color(0xFF171717))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tạm dừng", color = Color(0xFF171717))
                    }
                } else if (progress.status == AutoCallStatus.PAUSED) {
                    Button(
                        onClick = onResume,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF171717))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFFFFFFFF))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tiếp tục", color = Color(0xFFFFFFFF))
                    }
                }

                if (progress.status == AutoCallStatus.RUNNING || progress.status == AutoCallStatus.PAUSED) {
                    Button(
                        onClick = onStop,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEE0000))
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = Color(0xFFFFFFFF))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dừng hẳn", color = Color(0xFFFFFFFF))
                    }
                }
            }
        }
    }
}
