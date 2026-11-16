package com.gmail.nthai.presentation.components.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmail.nthai.presentation.viewmodel.ReportStats
import java.util.Locale

@Composable
fun ReportMetricHeader(
    stats: ReportStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReportMetricCard(
                modifier = Modifier.weight(1f),
                label = "Tổng tin",
                value = stats.total.toString(),
                icon = Icons.Default.Sms,
                containerColor = Color(0xFFFFFFFF),
                borderColor = Color(0xFFEBEBEB),
                labelColor = Color(0xFF666666),
                valueColor = Color(0xFF171717),
                iconTint = Color(0xFF171717)
            )
            ReportMetricCard(
                modifier = Modifier.weight(1f),
                label = "Thành công",
                value = stats.success.toString(),
                icon = Icons.Default.CheckCircle,
                containerColor = Color(0xFFF0FDF4),
                borderColor = Color(0xFFDCFCE7),
                labelColor = Color(0xFF166534),
                valueColor = Color(0xFF145C38),
                iconTint = Color(0xFF166534)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReportMetricCard(
                modifier = Modifier.weight(1f),
                label = "Thất bại",
                value = stats.failed.toString(),
                icon = Icons.Default.ErrorOutline,
                containerColor = Color(0xFFFEF2F2),
                borderColor = Color(0xFFFEE2E2),
                labelColor = Color(0xFF991B1B),
                valueColor = Color(0xFFC50000),
                iconTint = Color(0xFFDC2626)
            )
            ReportMetricCard(
                modifier = Modifier.weight(1f),
                label = "Tỷ lệ thành công",
                value = "${
                    String.format(
                        Locale.getDefault(),
                        "%.1f",
                        stats.successRatePercentage
                    )
                }%",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                containerColor = Color(0xFFEFF6FF),
                borderColor = Color(0xFFDBEAFE),
                labelColor = Color(0xFF1E40AF),
                valueColor = Color(0xFF0761D1),
                iconTint = Color(0xFF0070F3)
            )
        }
        if (stats.pending > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFFFEFCF))
                    .border(BorderStroke(1.dp, Color(0xFFFDE68A)), RoundedCornerShape(100.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = Color(0xFFAB570A),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Đang xử lý: Còn ${stats.pending} tin nhắn trong hàng chờ",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFFAB570A)
                )
            }
        }
    }
}

@Composable
fun ReportMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    borderColor: Color,
    labelColor: Color,
    valueColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = labelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = valueColor
            )
        }
    }
}
