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
    val dotColor: Color,
    val containerColor: Color,
    val borderColor: Color,
    val textColor: Color
)

@Composable
fun StatusBadge(
    status: LogStatus,
    modifier: Modifier = Modifier
) {
    val statusStyle = when (status) {
        LogStatus.SENT_SUCCESS -> StatusStyle(
            label = "Thành công",
            dotColor = Color(0xFF10B981),
            containerColor = Color(0xFFF0FDF4),
            borderColor = Color(0xFFDCFCE7),
            textColor = Color(0xFF166534)
        )

        LogStatus.FAILED -> StatusStyle(
            label = "Thất bại",
            dotColor = Color(0xFFEE0000),
            containerColor = Color(0xFFFEF2F2),
            borderColor = Color(0xFFFEE2E2),
            textColor = Color(0xFF991B1B)
        )

        LogStatus.PENDING -> StatusStyle(
            label = "Đang chờ",
            dotColor = Color(0xFFF5A623),
            containerColor = Color(0xFFFFEFCF),
            borderColor = Color(0xFFFDE68A),
            textColor = Color(0xFFAB570A)
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(statusStyle.containerColor)
            .border(1.dp, statusStyle.borderColor, RoundedCornerShape(100.dp))
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
            color = statusStyle.textColor
        )
    }
}
