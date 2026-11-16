package com.gmail.nthai.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gmail.nthai.domain.model.ImportMode
import com.gmail.nthai.presentation.components.common.SelectionOption
import com.gmail.nthai.presentation.components.common.SelectionOptionCard

@Composable
fun LoadingDialog(message: String) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFFFFF),
            border = BorderStroke(1.dp, Color(0xFFEBEBEB))
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFF171717),
                    strokeWidth = 4.dp
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Vui lòng đợi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF171717)
                    )
                    Text(
                        text = message.ifBlank { "Đang xử lý file Excel..." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
fun SelectPhoneColumnDialog(
    headers: List<String>,
    selectedHeader: String,
    onSelectHeader: (String) -> Unit,
    selectedImportMode: ImportMode = ImportMode.REPLACE_ALL,
    onSelectImportMode: (ImportMode) -> Unit = {},
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFFFFFF),
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                "Cấu hình nhập tệp Excel",
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "1. Chọn cột chứa Số điện thoại trong file:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717)
                )

                headers.forEach { header ->
                    SelectionOptionCard(
                        option = SelectionOption(
                            value = header,
                            title = header
                        ),
                        isSelected = (header == selectedHeader),
                        onSelect = { onSelectHeader(it) }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "2. Chọn chế độ lưu dữ liệu:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717)
                )

                ImportMode.entries.forEach { mode ->
                    SelectionOptionCard(
                        option = SelectionOption(
                            value = mode,
                            title = mode.label,
                            description = mode.description
                        ),
                        isSelected = (mode == selectedImportMode),
                        onSelect = { onSelectImportMode(it) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF171717),
                    contentColor = Color(0xFFFFFFFF)
                )
            ) {
                Text("Xác nhận nhập", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color(0xFF888888))
            }
        }
    )
}
