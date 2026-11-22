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
import com.gmail.nthai.util.PhoneNumberUtils
import com.gmail.nthai.presentation.components.common.AddCustomerDialog
import com.gmail.nthai.presentation.components.common.CustomerHeaderSummary
import com.gmail.nthai.presentation.components.common.CustomerSearchBarWithActions
import com.gmail.nthai.presentation.components.common.FilterChipItem
import com.gmail.nthai.presentation.components.common.GenericCustomerItemCard
import com.gmail.nthai.presentation.components.common.GenericCustomerItemData
import com.gmail.nthai.presentation.components.common.GenericFilterChipsBar

@Composable
fun ZaloRecipientTab(
    isAccessibilityEnabled: Boolean = false,
    recipients: List<ZaloRecipient> = emptyList(),
    blacklistedPhones: Set<String> = emptySet(),
    onAddRecipient: (name: String, phone: String) -> Unit = { _, _ -> },
    onDeleteRecipient: (id: Long) -> Unit = {},
    onAddToBlacklist: (phone: String) -> Unit = {},
    onRetryRecipient: (id: Long) -> Unit = {},
    onImportExcel: () -> Unit = {},
    onStartSending: () -> Unit = {},
    onOpenAccessibilitySettings: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "SUCCESS", "FAILED"
    var showAddDialog by remember { mutableStateOf(false) }

    var newCustomerName by remember { mutableStateOf("") }
    var newCustomerPhone by remember { mutableStateOf("") }

    val filteredList = recipients.filter { customer ->
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

    val totalCount = recipients.size
    val pendingCount = recipients.count { it.status == LogStatus.PENDING }
    val successCount = recipients.count { it.status == LogStatus.SENT_SUCCESS }
    val failedCount = recipients.count { it.status == LogStatus.FAILED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Accessibility Status Banner Card
        ZaloAccessibilityStatusCard(
            isAccessibilityEnabled = isAccessibilityEnabled,
            onOpenAccessibilitySettings = onOpenAccessibilitySettings
        )

        // Summary Header Card matching modern app style
        CustomerHeaderSummary(
            totalTitle = "TỔNG SỐ SĐT ZALO",
            totalCount = totalCount,
            successTitle = "ĐÃ GỬI THÀNH CÔNG",
            successCount = successCount
        )

        // Search Bar & Action Buttons
        CustomerSearchBarWithActions(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Tìm tên, SĐT...",
            onAddClick = { showAddDialog = true },
            onImportExcelClick = onImportExcel
        )

        // Filter Chips Bar
        val chips = listOf(
            FilterChipItem("ALL", "Tất cả", totalCount),
            FilterChipItem("PENDING", "Chờ gửi", pendingCount),
            FilterChipItem("SUCCESS", "Thành công", successCount),
            FilterChipItem("FAILED", "Thất bại", failedCount)
        )
        GenericFilterChipsBar(
            chips = chips,
            selectedKey = selectedFilter,
            onSelectKey = { selectedFilter = it }
        )

        // Recipient List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (recipients.isEmpty()) "Chưa có SĐT nào. Hãy thêm mới hoặc Nhập từ Excel" else "Không tìm thấy số điện thoại phù hợp",
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
                    val friendStatus = if (customer.isZaloFriend) "Bạn bè" else "Chưa kết bạn"
                    val normalized = remember(customer.phone) { PhoneNumberUtils.normalizePhoneNumber(customer.phone) }
                    val isBlacklisted = remember(normalized, blacklistedPhones) { blacklistedPhones.contains(normalized) }
                    GenericCustomerItemCard(
                        item = GenericCustomerItemData(
                            id = customer.id,
                            name = customer.name,
                            phone = customer.phone,
                            subtitle = friendStatus,
                            status = customer.status,
                            isSelected = false,
                            note = customer.note.ifBlank { null }
                        ),
                        onToggleSelection = null,
                        onDelete = { onDeleteRecipient(customer.id) },
                        onClick = null,
                        isBlacklisted = isBlacklisted,
                        onAddToBlacklist = { onAddToBlacklist(customer.phone) }
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
        AddCustomerDialog(
            title = "Thêm SĐT Zalo mới",
            onDismiss = { showAddDialog = false },
            onAddCustomer = { name, phone ->
                onAddRecipient(name, phone)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ZaloRecipientTabPreview() {
    MaterialTheme {
        ZaloRecipientTab(
            recipients = listOf(
                ZaloRecipient(
                    1,
                    "Nguyễn Văn Anh",
                    "0987654321",
                    isZaloFriend = true,
                    status = LogStatus.SENT_SUCCESS,
                    note = "Đã gửi tin & ảnh thành công"
                )
            ),
            onImportExcel = {}
        )
    }
}
