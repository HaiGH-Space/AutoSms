package com.gmail.nthai.presentation.components.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp

@Composable
fun EmptyReportState(
    hasAnyLogs: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFFAFAFA),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEBEBEB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
                    .border(1.dp, Color(0xFFEBEBEB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (hasAnyLogs) Icons.Default.FilterList else Icons.Default.Sms,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF171717)
                )
            }
            Text(
                if (hasAnyLogs) "Không có kết quả phù hợp" else "Chưa có lịch sử gửi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF171717)
            )
            Text(
                if (hasAnyLogs) "Thử đổi SIM hoặc chọn khoảng ngày khác để xem thêm bản ghi."
                else "Các kết quả gửi tin SMS tự động sẽ xuất hiện tại đây.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666)
            )
            if (hasAnyLogs) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onClearFilters,
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF171717)
                    )
                ) {
                    Text("Xóa bộ lọc", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
