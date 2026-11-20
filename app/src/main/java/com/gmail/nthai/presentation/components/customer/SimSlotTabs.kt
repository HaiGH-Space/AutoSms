package com.gmail.nthai.presentation.components.customer

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmail.nthai.domain.model.SimConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimSlotTabs(
    selectedSimSlot: Int,
    simConfigs: List<SimConfig>,
    onSelectSimSlot: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    SecondaryTabRow(
        selectedTabIndex = (selectedSimSlot - 1).coerceAtLeast(0),
        containerColor = Color(0xFFFFFFFF),
        contentColor = Color(0xFF171717),
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset((selectedSimSlot - 1).coerceAtLeast(0)),
                color = Color(0xFF171717)
            )
        },
        modifier = modifier
            .clip(RoundedCornerShape(9999.dp))
            .border(1.dp, Color(0xFFEBEBEB), RoundedCornerShape(9999.dp))
    ) {
        (1..2).forEach { slot ->
            val config = simConfigs.find { it.simSlot == slot }
            val label =
                if (!config?.carrierName.isNullOrBlank()) "SIM $slot (${config?.carrierName})" else "SIM $slot"
            Tab(
                selected = selectedSimSlot == slot,
                onClick = { onSelectSimSlot(slot) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.SimCard,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (selectedSimSlot == slot) Color(0xFF171717) else Color(0xFF888888)
                        )
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedSimSlot == slot) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedSimSlot == slot) Color(0xFF171717) else Color(0xFF888888)
                        )
                    }
                }
            )
        }
    }
}
