package com.gmail.nthai.presentation.components.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmail.nthai.presentation.viewmodel.CustomerCarrierFilter
import com.gmail.nthai.presentation.viewmodel.CustomerFilter

@Composable
fun CustomerFilterSection(
    selectedFilter: CustomerFilter,
    selectedCarrierFilter: CustomerCarrierFilter,
    onFilterChange: (CustomerFilter) -> Unit,
    onCarrierFilterChange: (CustomerCarrierFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CustomerFilterPill(
                selected = selectedFilter == CustomerFilter.ALL,
                text = "Tất cả",
                onClick = { onFilterChange(CustomerFilter.ALL) }
            )
            CustomerFilterPill(
                selected = selectedFilter == CustomerFilter.PENDING,
                text = "Chờ gửi",
                onClick = { onFilterChange(CustomerFilter.PENDING) }
            )
            CustomerFilterPill(
                selected = selectedFilter == CustomerFilter.SENT,
                text = "Thành công",
                onClick = { onFilterChange(CustomerFilter.SENT) }
            )
            CustomerFilterPill(
                selected = selectedFilter == CustomerFilter.FAILED,
                text = "Lỗi",
                onClick = { onFilterChange(CustomerFilter.FAILED) }
            )
        }

        // Carrier Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CustomerCarrierFilter.entries.forEach { carrierFilter ->
                CustomerFilterPill(
                    selected = selectedCarrierFilter == carrierFilter,
                    text = carrierFilter.label,
                    onClick = { onCarrierFilterChange(carrierFilter) }
                )
            }
        }
    }
}

@Composable
fun CustomerFilterPill(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9999.dp),
        color = if (selected) Color(0xFF171717) else Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, if (selected) Color(0xFF171717) else Color(0xFFEBEBEB)),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xFFFFFFFF) else Color(0xFF4D4D4D),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
