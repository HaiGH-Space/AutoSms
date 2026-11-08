package com.gmail.nthai.presentation.components.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gmail.nthai.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    state: DateRangePickerState,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .height(620.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                DateRangePicker(
                    state = state,
                    modifier = Modifier.weight(1f),
                    showModeToggle = false,
                    title = {
                        Text(
                            text = "Chọn khoảng ngày",
                            modifier = Modifier.padding(start = 12.dp, top = 8.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    headline = {
                        Text(
                            text = dateRangeHeadline(state),
                            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    TextButton(
                        onClick = onApply,
                        enabled = state.selectedStartDateMillis != null &&
                            state.selectedEndDateMillis != null
                    ) {
                        Text("Áp dụng")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun dateRangeHeadline(state: DateRangePickerState): String {
    val start = state.selectedStartDateMillis
    val end = state.selectedEndDateMillis
    return if (start == null || end == null) {
        "Chọn ngày bắt đầu và kết thúc"
    } else {
        "${DateTimeUtils.formatPickerDate(start)} – ${DateTimeUtils.formatPickerDate(end)}"
    }
}
