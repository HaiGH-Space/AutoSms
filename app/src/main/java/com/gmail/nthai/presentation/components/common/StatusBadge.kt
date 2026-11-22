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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
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
            dotColor = Color(0xFF0070F3),
            containerColor = Color(0xFFD3E5FF),
            borderColor = Color(0xFFD3E5FF),
            textColor = Color(0xFF0761D1)
        )

        LogStatus.FAILED -> StatusStyle(
            label = "Thất bại",
            dotColor = Color(0xFFEE0000),
            containerColor = Color(0xFFF7D4D6),
            borderColor = Color(0xFFF7D4D6),
            textColor = Color(0xFFC50000)
        )

        LogStatus.PENDING -> StatusStyle(
            label = "Đang chờ",
            dotColor = Color(0xFFF5A623),
            containerColor = Color(0xFFFFEFCF),
            borderColor = Color(0xFFFFEFCF),
            textColor = Color(0xFFAB570A)
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(statusStyle.containerColor)
            .border(1.dp, statusStyle.borderColor, RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(statusStyle.dotColor, CircleShape)
        )
        Text(
            text = statusStyle.label,
            style = TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            color = statusStyle.textColor
        )
    }
}
