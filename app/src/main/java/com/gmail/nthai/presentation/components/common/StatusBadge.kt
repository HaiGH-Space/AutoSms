package com.gmail.nthai.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmail.nthai.domain.model.LogStatus

data class StatusStyle(
    val label: String,
    val dotColor: Color
)

@Composable
fun StatusBadge(
    status: LogStatus,
    modifier: Modifier = Modifier
) {
    val statusStyle = when (status) {
        LogStatus.SENT_SUCCESS -> StatusStyle(
            label = "Thành công",
            dotColor = Color(0xFF10B981)
        )

        LogStatus.FAILED -> StatusStyle(
            label = "Thất bại",
            dotColor = Color(0xFFEE0000)
        )

        LogStatus.PENDING -> StatusStyle(
            label = "Đang chờ",
            dotColor = Color(0xFFF5A623)
        )
    }

    Row(
        modifier = modifier
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
                .background(statusStyle.dotColor, CircleShape)
        )
        Text(
            text = statusStyle.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
