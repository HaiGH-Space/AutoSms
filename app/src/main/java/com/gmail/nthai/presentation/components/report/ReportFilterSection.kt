package com.gmail.nthai.presentation.components.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Icon
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

@Composable
fun ReportFilterSection(
    filteredCount: Int,
    totalCount: Int,
    selectedSimSlot: Int?,
    dateRangeLabel: String,
    hasDateRange: Boolean,
    onSelectSimSlot: (Int?) -> Unit,
    onOpenDatePicker: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Lịch sử gửi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFFF5F5F5))
                        .border(1.dp, Color(0xFFEBEBEB), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$filteredCount / $totalCount bản ghi",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF4D4D4D)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReportPillFilterChip(
                text = "Tất cả SIM",
                selected = selectedSimSlot == null,
                onClick = { onSelectSimSlot(null) },
                leadingIcon = if (selectedSimSlot == null) {
                    {
                        Icon(
                            Icons.Default.SimCard,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFFFFFF)
                        )
                    }
                } else null
            )
            ReportPillFilterChip(
                text = "SIM 1",
                selected = selectedSimSlot == 1,
                onClick = { onSelectSimSlot(1) }
            )
            ReportPillFilterChip(
                text = "SIM 2",
                selected = selectedSimSlot == 2,
                onClick = { onSelectSimSlot(2) }
            )
            ReportPillFilterChip(
                text = dateRangeLabel,
                selected = hasDateRange,
                onClick = onOpenDatePicker,
                leadingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (hasDateRange) Color(0xFFFFFFFF) else Color(0xFF666666)
                    )
                }
            )

            if (selectedSimSlot != null || hasDateRange) {
                ReportPillFilterChip(
                    text = "Xóa lọc",
                    selected = false,
                    onClick = onClearFilters,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFEE0000)
                        )
                    },
                    textColor = Color(0xFFEE0000),
                    borderColor = Color(0xFFF7D4D6),
                    containerColor = Color(0xFFFEF2F2)
                )
            }
        }
    }
}

@Composable
fun ReportPillFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    textColor: Color = if (selected) Color(0xFFFFFFFF) else Color(0xFF4D4D4D),
    borderColor: Color = if (selected) Color(0xFF171717) else Color(0xFFEBEBEB),
    containerColor: Color = if (selected) Color(0xFF171717) else Color(0xFFFFFFFF)
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (leadingIcon != null) {
                leadingIcon()
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = textColor,
                maxLines = 1
            )
        }
    }
}
