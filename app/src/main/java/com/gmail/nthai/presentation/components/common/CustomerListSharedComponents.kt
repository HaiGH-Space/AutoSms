package com.gmail.nthai.presentation.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.gmail.nthai.presentation.components.customer.CustomerSelectionBar

@Composable
fun CustomerHeaderSummary(
    totalTitle: String = "TỔNG SỐ KHÁCH HÀNG",
    totalCount: Int,
    successTitle: String = "ĐÃ HOÀN THÀNH",
    successCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, Color(0xFFEBEBEB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    tint = Color(0xFF171717)
                )
                Column {
                    Text(
                        totalTitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF888888)
                    )
                    Text(
                        totalCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF171717)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    successTitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF888888)
                )
                Text(
                    "$successCount / $totalCount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF171717)
                )
            }
        }
    }
}

@Composable
fun CustomerSearchBarWithActions(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Tìm theo tên, số điện thoại...",
    onAddClick: (() -> Unit)? = null,
    onImportExcelClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AutoSmsSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = placeholder
        )

        if (onAddClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF171717), RoundedCornerShape(9999.dp))
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Thêm SĐT",
                    tint = Color(0xFFFFFFFF),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (onImportExcelClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onImportExcelClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(9999.dp))
            ) {
                Icon(
                    Icons.Default.FileUpload,
                    contentDescription = "Excel",
                    tint = Color(0xFF171717),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onAddCustomer: (name: String, phone: String) -> Unit,
    title: String = "Thêm khách hàng"
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên người nhận (tùy chọn)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(9999.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(9999.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phone.isNotBlank()) {
                        onAddCustomer(name.trim(), phone.trim())
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF171717)),
                shape = RoundedCornerShape(9999.dp)
            ) {
                Text("Thêm", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(9999.dp)
            ) {
                Text("Hủy", color = Color(0xFF171717), fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}
