package com.gmail.nthai.presentation.components.autocall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmail.nthai.domain.model.CallLogRecord
import com.gmail.nthai.presentation.components.common.AutoSmsCardContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AutoCallLogSection(
    logs: List<CallLogRecord>,
    onClearLogs: () -> Unit
) {
    AutoSmsCardContainer(
        title = "Nhật ký cuộc gọi (${logs.size})",
        titleAction = {
            if (logs.isNotEmpty()) {
                TextButton(onClick = onClearLogs) {
                    Text("Xóa lịch sử", color = Color(0xFFD32F2F))
                }
            }
        }
    ) {
        if (logs.isEmpty()) {
            Text(
                text = "Chưa có cuộc gọi nào được thực hiện.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                logs.take(20).forEachIndexed { index, log ->
                    CallLogItem(log = log)
                    if (index < logs.size - 1 && index < 19) {
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                    }
                }
            }
        }
    }
}

@Composable
private fun CallLogItem(log: CallLogRecord) {
    val dateFormat = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(log.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.customerName.ifBlank { log.phone },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${log.phone} • $formattedTime",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = when (log.status) {
                "COMPLETED" -> Color(0xFFE8F5E9)
                "DIALING" -> Color(0xFFE3F2FD)
                else -> Color(0xFFFFEBEE)
            }
        ) {
            Text(
                text = when (log.status) {
                    "COMPLETED" -> "Đã gọi (${log.callDurationSeconds}s)"
                    "DIALING" -> "Đang gọi..."
                    else -> "Lỗi/Bỏ qua"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = when (log.status) {
                    "COMPLETED" -> Color(0xFF2E7D32)
                    "DIALING" -> Color(0xFF1565C0)
                    else -> Color(0xFFC62828)
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
