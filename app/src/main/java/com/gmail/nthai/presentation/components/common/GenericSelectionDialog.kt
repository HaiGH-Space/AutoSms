package com.gmail.nthai.presentation.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SelectionOption<T>(
    val value: T,
    val title: String,
    val description: String? = null
)

@Composable
fun <T> SelectionOptionCard(
    option: SelectionOption<T>,
    isSelected: Boolean,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFF171717) else Color(0xFFEBEBEB)
    val cardBackground = if (isSelected) Color(0xFFFAFAFA) else Color(0xFFFFFFFF)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect(option.value) },
        shape = RoundedCornerShape(12.dp),
        color = cardBackground,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = { onSelect(option.value) },
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF171717),
                    unselectedColor = Color(0xFF888888)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = Color(0xFF171717)
                )
                if (!option.description.isNullOrBlank()) {
                    Text(
                        text = option.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
fun <T> GenericSelectionDialog(
    title: String,
    options: List<SelectionOption<T>>,
    initialSelected: T,
    onConfirm: (T) -> Unit,
    onDismiss: () -> Unit,
    description: String? = null,
    confirmText: String = "Xác nhận",
    dismissText: String = "Hủy"
) {
    var selectedOption by remember { mutableStateOf(initialSelected) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFFFFFFFF),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF171717)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4D4D4D)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                options.forEach { option ->
                    SelectionOptionCard(
                        option = option,
                        isSelected = option.value == selectedOption,
                        onSelect = { selectedOption = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedOption) },
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF171717),
                    contentColor = Color(0xFFFFFFFF)
                )
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = Color(0xFF888888))
            }
        }
    )
}
