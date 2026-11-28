package com.gmail.nthai.presentation.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FilterChipItem(
    val key: String,
    val label: String,
    val count: Int? = null
)

@Composable
fun GenericFilterChipsBar(
    chips: List<FilterChipItem>,
    selectedKey: String,
    onSelectKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        chips.forEach { chip ->
            val isSelected = selectedKey == chip.key
            val displayLabel = if (chip.count != null) "${chip.label} (${chip.count})" else chip.label
            FilterChip(
                selected = isSelected,
                onClick = { onSelectKey(chip.key) },
                label = {
                    Text(
                        displayLabel,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF171717),
                    selectedLabelColor = Color(0xFFFFFFFF),
                    containerColor = Color(0xFFFFFFFF),
                    labelColor = Color(0xFF171717)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) Color(0xFF171717) else Color(0xFFEBEBEB),
                    selectedBorderColor = Color(0xFF171717),
                    borderWidth = 1.dp
                )
            )
        }
    }
}
