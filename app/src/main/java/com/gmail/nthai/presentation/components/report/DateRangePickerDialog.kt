package com.gmail.nthai.presentation.components.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmail.nthai.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    state: DateRangePickerState,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onApply,
                enabled = state.selectedStartDateMillis != null &&
                        state.selectedEndDateMillis != null,
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF171717),
                    contentColor = Color(0xFFFFFFFF),
                    disabledContainerColor = Color(0xFFEBEBEB),
                    disabledContentColor = Color(0xFFA1A1A1)
                )
            ) {
                Text("Áp dụng", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(100.dp),
                border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF171717)
                )
            ) {
                Text("Hủy", fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(24.dp),
        colors = DatePickerDefaults.colors(
            containerColor = Color(0xFFFFFFFF)
        )
    ) {
        DateRangePicker(
            state = state,
            modifier = Modifier.weight(1f),
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFFFFFFFF),
                selectedDayContainerColor = Color(0xFF171717),
                selectedDayContentColor = Color(0xFFFFFFFF),
                dayInSelectionRangeContainerColor = Color(0xFFF5F5F5),
                dayInSelectionRangeContentColor = Color(0xFF171717),
                todayDateBorderColor = Color(0xFF171717),
                todayContentColor = Color(0xFF171717),
                headlineContentColor = Color(0xFF171717),
                titleContentColor = Color(0xFF666666)
            ),
            title = {
                Text(
                    text = "Chọn khoảng ngày",
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717)
                )
            },
            headline = {
                Text(
                    text = dateRangeHeadline(state),
                    modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun dateRangeHeadline(state: DateRangePickerState): String {
    val start = state.selectedStartDateMillis
    val end = state.selectedEndDateMillis
    return if (start == null || end == null) {
        "Vui lòng chọn ngày bắt đầu và kết thúc"
    } else {
        "${DateTimeUtils.formatPickerDate(start)} – ${DateTimeUtils.formatPickerDate(end)}"
    }
}
