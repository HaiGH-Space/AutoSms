package com.gmail.nthai.presentation.components.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmail.nthai.domain.model.SimConfig

@Composable
fun ModernSimCard(
    simSlot: Int,
    config: SimConfig?,
    assignedCount: Int,
    readyCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val detected = config?.subscriptionId != null && config.subscriptionId != -1
    val carrierName = config?.carrierName?.trim().orEmpty()
    val dailyLimit = config?.dailyLimit ?: 0
    val sentToday = config?.sentCountToday ?: 0
    val quotaProgress =
        if (dailyLimit > 0) (sentToday.toFloat() / dailyLimit).coerceIn(0f, 1f) else 0f

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, Color(0xFFEBEBEB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = Color(0xFFF5F5F5),
                    border = BorderStroke(1.dp, Color(0xFFEBEBEB))
                ) {
                    Text(
                        text = "SIM $simSlot",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF171717),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            if (detected) Color(0xFF10B981) else Color(0xFF888888),
                            CircleShape
                        )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (carrierName.isNotBlank()) carrierName else if (detected) "Đã nhận diện" else "Chưa lắp SIM",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF171717),
                    maxLines = 1
                )
                Text(
                    text = "$assignedCount liên hệ · $readyCount chờ",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp
                    ),
                    color = Color(0xFF888888)
                )
            }

            if (detected && dailyLimit > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Hạn mức hôm nay",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF888888)
                        )
                        Text(
                            text = "$sentToday/$dailyLimit",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF171717)
                        )
                    }
                    LinearProgressIndicator(
                        progress = { quotaProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(9999.dp)),
                        color = if (quotaProgress >= 0.9f) Color(0xFFEE0000) else Color(0xFF171717),
                        trackColor = Color(0xFFEBEBEB)
                    )
                }
            }
        }
    }
}
