package com.gmail.nthai.presentation.components.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomerSelectionBar(
    allSelected: Boolean,
    selectedCount: Int,
    totalCount: Int,
    onSelectAllChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onSelectAllChange(!allSelected) }
        ) {
            Checkbox(
                checked = allSelected,
                onCheckedChange = { onSelectAllChange(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF171717),
                    checkmarkColor = Color(0xFFFFFFFF),
                    uncheckedColor = Color(0xFF888888)
                )
            )
            Text(
                text = if (allSelected) "Bỏ chọn tất cả" else "Chọn tất cả",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF171717)
            )
        }

        Surface(
            shape = RoundedCornerShape(9999.dp),
            color = Color(0xFFFFFFFF),
            border = BorderStroke(1.dp, Color(0xFFEBEBEB))
        ) {
            Text(
                text = "Đã chọn: $selectedCount / $totalCount",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF171717),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}
