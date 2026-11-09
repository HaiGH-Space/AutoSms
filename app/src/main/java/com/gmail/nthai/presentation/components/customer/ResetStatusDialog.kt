package com.gmail.nthai.presentation.components.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmail.nthai.domain.model.ResetStatusScope

@Composable
fun ResetStatusDialog(
    onConfirm: (ResetStatusScope) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedScope by remember { mutableStateOf(ResetStatusScope.ALL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Đặt lại trạng thái gửi tin",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Chọn phạm vi đặt lại trạng thái về 'Chưa gửi' để tiếp tục phát tin nhắn mà không cần xóa danh sách:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                ResetStatusScope.entries.forEach { scope ->
                    val isSelected = scope == selectedScope
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedScope = scope },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedScope = scope }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = scope.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = when (scope) {
                                        ResetStatusScope.ALL -> "Đặt lại toàn bộ danh sách về Chưa gửi (Xóa toàn bộ lịch sử gửi cũ)"
                                        ResetStatusScope.FAILED_ONLY -> "Chỉ chuyển các tin nhắn LỖI về Chưa gửi để thử lại (Giữ nguyên lịch sử tin thành công)"
                                        ResetStatusScope.SENT_ONLY -> "Chuyển các tin ĐÃ GỬI về Chưa gửi và cho phép gửi lại (Xóa lịch sử nhóm này)"
                                        ResetStatusScope.SELECTED_ONLY -> "Đặt lại cho các khách hàng đang được tích chọn và cho phép gửi lại"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedScope) }) {
                Text("Xác nhận đặt lại")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
