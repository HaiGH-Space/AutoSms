package com.gmail.nthai.presentation.components.autocall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmail.nthai.domain.model.SimConfig
import com.gmail.nthai.presentation.components.common.AutoSmsCardContainer

@Composable
fun AutoCallConfigCard(
    delaySeconds: Int,
    onDelayChange: (Int) -> Unit,
    selectedSimSlot: Int,
    onSimSlotChange: (Int) -> Unit,
    simConfigs: List<SimConfig>,
    totalCustomersCount: Int,
    selectedCustomersCount: Int,
    isRunning: Boolean,
    onStartClick: () -> Unit
) {
    AutoSmsCardContainer(title = "Cấu hình tự động gọi") {
        Column(modifier = Modifier.fillMaxWidth()) {
            // SIM Selection Row
            Text(
                text = "Chọn SIM thực hiện cuộc gọi:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4D4D4D)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                (1..2).forEach { slot ->
                    val isSelected = selectedSimSlot == slot
                    val simInfo = simConfigs.find { it.simSlot == slot }
                    val carrierLabel = simInfo?.carrierName?.takeIf { it.isNotBlank() } ?: "SIM $slot"

                    Surface(
                        onClick = { if (!isRunning) onSimSlotChange(slot) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF171717) else Color(0xFFFFFFFF),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF171717) else Color(0xFFEBEBEB)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SimCard,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF171717)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "SIM $slot",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF171717)
                                )
                                Text(
                                    text = carrierLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color(0xFFEBEBEB) else Color(0xFF888888)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thời gian nghỉ giữa các cuộc gọi:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4D4D4D)
                )
                Text(
                    text = "${delaySeconds}s",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Slider(
                value = delaySeconds.toFloat(),
                onValueChange = { onDelayChange(it.toInt()) },
                valueRange = 1f..30f,
                steps = 29,
                enabled = !isRunning,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selectedCustomersCount > 0)
                        "Đã chọn: $selectedCustomersCount / $totalCustomersCount danh bạ"
                    else
                        "Sẽ gọi toàn bộ: $totalCustomersCount danh bạ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartClick,
                enabled = !isRunning && totalCustomersCount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF171717),
                    disabledContainerColor = Color(0xFFEBEBEB)
                )
            ) {
                Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = Color(0xFFFFFFFF))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bắt đầu tự động gọi (SIM $selectedSimSlot)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF)
                )
            }
        }
    }
}
