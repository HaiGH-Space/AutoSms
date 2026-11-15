package com.gmail.nthai.presentation.components.zalo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.model.ZaloRecipient
import com.gmail.nthai.presentation.components.common.AutoSmsCardContainer
import com.gmail.nthai.presentation.components.common.AutoSmsSearchBar
import com.gmail.nthai.presentation.components.common.StatusBadge

@Composable
fun ZaloRecipientTab(
    recipients: List<ZaloRecipient> = emptyList(),
    onAddRecipient: (name: String, phone: String) -> Unit = { _, _ -> },
    onDeleteRecipient: (id: Long) -> Unit = {},
    onRetryRecipient: (id: Long) -> Unit = {},
    onImportExcel: () -> Unit = {},
    onStartSending: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "SUCCESS", "FAILED"
    var showAddDialog by remember { mutableStateOf(false) }

    var newCustomerName by remember { mutableStateOf("") }
    var newCustomerPhone by remember { mutableStateOf("") }

    val displayList =
        if (recipients.isEmpty() && searchQuery.isEmpty() && selectedFilter == "ALL") {
            remember {
                listOf(
                    ZaloRecipient(
                        1,
                        "Nguyễn Văn Anh",
                        "0987654321",
                        isZaloFriend = true,
                        status = LogStatus.SENT_SUCCESS,
                        note = "Đã gửi tin & ảnh thành công"
                    ),
                    ZaloRecipient(
                        2,
                        "Trần Thị Bình",
                        "0912345678",
                        isZaloFriend = false,
                        status = LogStatus.PENDING,
                        note = "Đã bật tự động kết bạn"
                    ),
                    ZaloRecipient(
                        3,
                        "Lê Văn Cường",
                        "0933445566",
                        isZaloFriend = true,
                        status = LogStatus.FAILED,
                        note = "SĐT chưa mở tài khoản Zalo"
                    ),
                    ZaloRecipient(
                        4,
                        "Phạm Minh Đức",
                        "0977889900",
                        isZaloFriend = false,
                        status = LogStatus.PENDING,
                        note = "Chờ gửi"
                    )
                )
            }
        } else {
            recipients
        }

    val filteredList = displayList.filter { customer ->
        val matchesSearch =
            customer.name.contains(searchQuery, ignoreCase = true) || customer.phone.contains(
                searchQuery
            )
        val matchesFilter = when (selectedFilter) {
            "PENDING" -> customer.status == LogStatus.PENDING
            "SUCCESS" -> customer.status == LogStatus.SENT_SUCCESS
            "FAILED" -> customer.status == LogStatus.FAILED
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val totalCount = displayList.size
    val pendingCount = displayList.count { it.status == LogStatus.PENDING }
    val successCount = displayList.count { it.status == LogStatus.SENT_SUCCESS }
    val failedCount = displayList.count { it.status == LogStatus.FAILED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Summary Header Card matching modern app style
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
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
                            "TỔNG SỐ SĐT ZALO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF888888)
                        )
                        Text(
                            totalCount.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF171717)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "ĐÃ GỬI THÀNH CÔNG",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF888888)
                    )
                    Text(
                        "$successCount / $totalCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF171717)
                    )
                }
            }
        }

        // Search Bar & Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AutoSmsSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = "Tìm tên, SĐT..."
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF171717), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Thêm SĐT",
                    tint = Color(0xFFFFFFFF),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onImportExcel,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    Icons.Default.FileUpload,
                    contentDescription = "Excel",
                    tint = Color(0xFF171717),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Single Horizontal Scrollable Row for Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val chips = listOf(
                "ALL" to "Tất cả ($totalCount)",
                "PENDING" to "Chờ gửi ($pendingCount)",
                "SUCCESS" to "Thành công ($successCount)",
                "FAILED" to "Thất bại ($failedCount)"
            )
            chips.forEach { (key, label) ->
                val selected = selectedFilter == key
                FilterChip(
                    selected = selected,
                    onClick = { selectedFilter = key },
                    label = {
                        Text(
                            label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
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
                        selected = selected,
                        borderColor = Color(0xFFEBEBEB),
                        selectedBorderColor = Color(0xFF171717)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Recipient List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không tìm thấy số điện thoại phù hợp",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF888888)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id }) { customer ->
                    ZaloCustomerCard(
                        customer = customer,
                        onDelete = { onDeleteRecipient(customer.id) },
                        onRetry = { onRetryRecipient(customer.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Prominent "Bắt đầu gửi Zalo" Action Button
        Button(
            onClick = onStartSending,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(9999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF171717),
                contentColor = Color(0xFFFFFFFF)
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Bắt Đầu Gửi Zalo (${filteredList.size} SĐT)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }

    // Add Customer Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    "Thêm SĐT Zalo mới",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newCustomerName,
                        onValueChange = { newCustomerName = it },
                        label = { Text("Tên người nhận") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newCustomerPhone,
                        onValueChange = { newCustomerPhone = it },
                        label = { Text("Số điện thoại Zalo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCustomerPhone.isNotBlank()) {
                            onAddRecipient(newCustomerName, newCustomerPhone)
                            newCustomerName = ""
                            newCustomerPhone = ""
                            showAddDialog = false
                        }
                    },
                    shape = RoundedCornerShape(9999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF171717),
                        contentColor = Color(0xFFFFFFFF)
                    )
                ) {
                    Text("Thêm SĐT", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddDialog = false },
                    shape = RoundedCornerShape(9999.dp),
                    border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF171717))
                ) {
                    Text("Hủy")
                }
            },
            containerColor = Color(0xFFFFFFFF),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ZaloCustomerCard(
    customer: ZaloRecipient,
    onDelete: () -> Unit,
    onRetry: () -> Unit
) {
    AutoSmsCardContainer(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.firstOrNull()?.uppercase() ?: "?",
                    color = Color(0xFF171717),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF171717),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (customer.isZaloFriend) "(Bạn bè)" else "(Chưa kết bạn)",
                        fontSize = 11.sp,
                        color = if (customer.isZaloFriend) Color(0xFF10B981) else Color(0xFF888888)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = customer.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888)
                )
                if (customer.note.isNotBlank()) {
                    Text(
                        text = customer.note,
                        fontSize = 11.sp,
                        color = when (customer.status) {
                            LogStatus.FAILED -> Color(0xFFEE0000)
                            LogStatus.SENT_SUCCESS -> Color(0xFF10B981)
                            else -> Color(0xFF888888)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status = customer.status)
                Spacer(modifier = Modifier.height(4.dp))

                if (customer.status == LogStatus.FAILED) {
                    IconButton(onClick = onRetry, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Gửi lại",
                            tint = Color(0xFF171717),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Xóa",
                            tint = Color(0xFF888888),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ZaloRecipientTabPreview() {
    MaterialTheme {
        ZaloRecipientTab(onImportExcel = {})
    }
}
