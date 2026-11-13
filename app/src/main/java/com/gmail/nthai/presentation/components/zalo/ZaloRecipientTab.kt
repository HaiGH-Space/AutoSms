package com.gmail.nthai.presentation.components.zalo

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.gmail.nthai.presentation.components.common.StatusBadge

data class ZaloCustomerItem(
    val id: String,
    val name: String,
    val phone: String,
    val isZaloFriend: Boolean,
    val status: LogStatus,
    val note: String = ""
)

@Composable
fun ZaloRecipientTab(
    onImportExcel: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "SUCCESS", "FAILED"
    var showAddDialog by remember { mutableStateOf(false) }

    var newCustomerName by remember { mutableStateOf("") }
    var newCustomerPhone by remember { mutableStateOf("") }

    val customerList = remember {
        mutableStateListOf(
            ZaloCustomerItem("1", "Nguyễn Văn Anh", "0987654321", isZaloFriend = true, status = LogStatus.SENT_SUCCESS, note = "Đã gửi tin & ảnh thành công"),
            ZaloCustomerItem("2", "Trần Thị Bình", "0912345678", isZaloFriend = false, status = LogStatus.PENDING, note = "Đã bật tự động kết bạn"),
            ZaloCustomerItem("3", "Lê Văn Cường", "0933445566", isZaloFriend = true, status = LogStatus.FAILED, note = "SĐT chưa mở tài khoản Zalo"),
            ZaloCustomerItem("4", "Phạm Minh Đức", "0977889900", isZaloFriend = false, status = LogStatus.PENDING, note = "Chờ gửi"),
            ZaloCustomerItem("5", "Hoàng Kim Ngân", "0909123456", isZaloFriend = true, status = LogStatus.SENT_SUCCESS, note = "Đã gửi qua Trợ năng Zalo"),
            ZaloCustomerItem("6", "Đỗ Hữu Tài", "0944556677", isZaloFriend = true, status = LogStatus.PENDING, note = "Chờ gửi")
        )
    }

    val filteredList = customerList.filter { customer ->
        val matchesSearch = customer.name.contains(searchQuery, ignoreCase = true) || customer.phone.contains(searchQuery)
        val matchesFilter = when (selectedFilter) {
            "PENDING" -> customer.status == LogStatus.PENDING
            "SUCCESS" -> customer.status == LogStatus.SENT_SUCCESS
            "FAILED" -> customer.status == LogStatus.FAILED
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val totalCount = customerList.size
    val pendingCount = customerList.count { it.status == LogStatus.PENDING }
    val successCount = customerList.count { it.status == LogStatus.SENT_SUCCESS }
    val failedCount = customerList.count { it.status == LogStatus.FAILED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Summary Header Card matching CustomerListScreen style
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
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
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            "TỔNG SỐ SĐT ZALO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            totalCount.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "ĐÃ GỬI THÀNH CÔNG",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "$successCount / $totalCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Search Bar & Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                placeholder = { Text("Tìm theo tên hoặc SĐT...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Thêm SĐT",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onImportExcel,
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    Icons.Default.FileUpload,
                    contentDescription = "Excel",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Single Horizontal Scrollable Row for Filter Chips (Matching CustomerListScreen)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("Tất cả ($totalCount)") }
            )
            FilterChip(
                selected = selectedFilter == "PENDING",
                onClick = { selectedFilter = "PENDING" },
                label = { Text("Chờ gửi ($pendingCount)") }
            )
            FilterChip(
                selected = selectedFilter == "SUCCESS",
                onClick = { selectedFilter = "SUCCESS" },
                label = { Text("Thành công ($successCount)") }
            )
            FilterChip(
                selected = selectedFilter == "FAILED",
                onClick = { selectedFilter = "FAILED" },
                label = { Text("Thất bại ($failedCount)") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        onDelete = { customerList.remove(customer) },
                        onRetry = {
                            val index = customerList.indexOf(customer)
                            if (index >= 0) {
                                customerList[index] = customer.copy(status = LogStatus.PENDING, note = "Chờ gửi lại")
                            }
                        }
                    )
                }
            }
        }
    }

    // Add Customer Dialog Simulation
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Thêm SĐT Zalo mới", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCustomerName,
                        onValueChange = { newCustomerName = it },
                        label = { Text("Tên người nhận") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newCustomerPhone,
                        onValueChange = { newCustomerPhone = it },
                        label = { Text("Số điện thoại Zalo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCustomerPhone.isNotBlank()) {
                            customerList.add(
                                ZaloCustomerItem(
                                    id = System.currentTimeMillis().toString(),
                                    name = newCustomerName.ifBlank { "Khách hàng Mới" },
                                    phone = newCustomerPhone,
                                    isZaloFriend = false,
                                    status = LogStatus.PENDING,
                                    note = "Chờ gửi"
                                )
                            )
                            newCustomerName = ""
                            newCustomerPhone = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Thêm SĐT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun ZaloCustomerCard(
    customer: ZaloCustomerItem,
    onDelete: () -> Unit,
    onRetry: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (customer.isZaloFriend) "(Bạn bè)" else "(Chưa kết bạn)",
                        fontSize = 11.sp,
                        color = if (customer.isZaloFriend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = customer.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (customer.note.isNotBlank()) {
                    Text(
                        text = customer.note,
                        fontSize = 11.sp,
                        color = when (customer.status) {
                            LogStatus.FAILED -> MaterialTheme.colorScheme.error
                            LogStatus.SENT_SUCCESS -> Color(0xFF176B45)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Xóa",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
