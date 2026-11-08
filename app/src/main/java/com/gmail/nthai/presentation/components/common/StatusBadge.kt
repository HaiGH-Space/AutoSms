package com.gmail.nthai.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmail.nthai.domain.model.LogStatus

data class StatusStyle(
    val label: String,
    val color: Color,
    val containerColor: Color,
    val icon: ImageVector
)

@Composable
fun StatusBadge(
    status: LogStatus,
    modifier: Modifier = Modifier
) {
    val statusStyle = when (status) {
        LogStatus.SENT_SUCCESS -> StatusStyle(
            label = "Thành công",
            color = Color(0xFF176B45),
            containerColor = Color(0xFFE0F2E8),
            icon = Icons.Default.CheckCircle
        )
        LogStatus.FAILED -> StatusStyle(
            label = "Thất bại",
            color = MaterialTheme.colorScheme.error,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            icon = Icons.Default.ErrorOutline
        )
        LogStatus.PENDING -> StatusStyle(
            label = "Đang chờ",
            color = MaterialTheme.colorScheme.secondary,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            icon = Icons.Default.Schedule
        )
    }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(statusStyle.containerColor)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = statusStyle.icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = statusStyle.color
        )
        Text(
            text = statusStyle.label,
            style = MaterialTheme.typography.labelSmall,
            color = statusStyle.color,
            fontWeight = FontWeight.SemiBold
        )
    }
}
