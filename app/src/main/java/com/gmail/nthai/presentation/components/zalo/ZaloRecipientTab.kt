package com.gmail.nthai.presentation.components.zalo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.model.ZaloRecipient
import com.gmail.nthai.presentation.components.common.CampaignBottomActionBar
import com.gmail.nthai.presentation.components.common.FilterChipItem
import com.gmail.nthai.presentation.components.common.GenericCustomerItemData
import com.gmail.nthai.presentation.components.common.GenericRecipientListContent

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
    onOpenAccessibilitySettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredList = recipients.filter { customer ->
        val matchesSearch = customer.name.contains(searchQuery, ignoreCase = true) ||
                customer.phone.contains(searchQuery)
        val matchesFilter = when (selectedFilter) {
            "PENDING" -> customer.status == LogStatus.PENDING
            "SUCCESS" -> customer.status == LogStatus.SENT_SUCCESS
            "FAILED" -> customer.status == LogStatus.FAILED
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val totalCount = recipients.size
    val successCount = recipients.count { it.status == LogStatus.SENT_SUCCESS }
    val pendingCount = recipients.count { it.status == LogStatus.PENDING }
    val failedCount = recipients.count { it.status == LogStatus.FAILED }

    val filterChips = listOf(
        FilterChipItem("ALL", "Tất cả", totalCount),
        FilterChipItem("PENDING", "Chờ gửi", pendingCount),
        FilterChipItem("SUCCESS", "Thành công", successCount),
        FilterChipItem("FAILED", "Thất bại", failedCount)
    )

    val genericItems = filteredList.map { recipient ->
        GenericCustomerItemData(
            id = recipient.id,
            name = recipient.name,
            phone = recipient.phone,
            subtitle = if (recipient.isZaloFriend) "Đã kết bạn Zalo" else "Chưa kết bạn Zalo",
            status = recipient.status,
            note = recipient.note
        )
    }

    GenericRecipientListContent(
        totalTitle = "TỔNG SỐ SĐT ZALO",
        totalCount = totalCount,
        successTitle = "ĐÃ GỬI ZALO THÀNH CÔNG",
        successCount = successCount,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        searchPlaceholder = "Tìm tên, SĐT Zalo...",
        selectedFilter = selectedFilter,
        onFilterChange = { selectedFilter = it },
        filterChips = filterChips,
        items = genericItems,
        blacklistedPhones = blacklistedPhones,
        onAddRecipient = onAddRecipient,
        onDeleteRecipient = onDeleteRecipient,
        onAddToBlacklist = onAddToBlacklist,
        onRetryRecipient = onRetryRecipient,
        onImportExcel = onImportExcel,
        addDialogTitle = "Thêm SĐT Zalo",
        modifier = modifier,
        emptyStateMessage = "Chưa có danh sách SĐT Zalo nào.",
        bottomBar = {
            if (recipients.isNotEmpty()) {
                CampaignBottomActionBar(
                    primaryButtonText = "Bắt đầu gửi Zalo",
                    primaryButtonIcon = Icons.AutoMirrored.Filled.Send,
                    onPrimaryClick = onStartSending,
                    enabled = true
                )
            }
        }
    )
}
