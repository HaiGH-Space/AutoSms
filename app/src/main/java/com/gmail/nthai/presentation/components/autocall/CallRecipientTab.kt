package com.gmail.nthai.presentation.components.autocall

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gmail.nthai.domain.model.CallRecipient
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.repository.CallRecipientStats
import com.gmail.nthai.presentation.components.common.CampaignBottomActionBar
import com.gmail.nthai.presentation.components.common.FilterChipItem
import com.gmail.nthai.presentation.components.common.GenericCustomerItemData
import com.gmail.nthai.presentation.components.common.GenericRecipientListContent

@Composable
fun CallRecipientTab(
    recipients: List<CallRecipient>,
    stats: CallRecipientStats,
    blacklistedPhones: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onToggleSelection: (id: Long, currentSelection: Boolean) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onAddRecipient: (name: String, phone: String) -> Unit,
    onDeleteRecipient: (id: Long) -> Unit,
    onDeleteAllRecipients: () -> Unit,
    onResetStatus: () -> Unit,
    onAddToBlacklist: (String) -> Unit,
    onImportExcel: () -> Unit,
    modifier: Modifier = Modifier,
    selectedSimSlot: Int = 1,
    onStartCampaign: () -> Unit = {}
) {
    val filteredList = recipients.filter { recipient ->
        val matchesSearch = recipient.name.contains(searchQuery, ignoreCase = true) ||
                recipient.phone.contains(searchQuery)
        val matchesFilter = when (selectedFilter) {
            "PENDING" -> recipient.status == LogStatus.PENDING
            "SUCCESS" -> recipient.status == LogStatus.SENT_SUCCESS
            "FAILED" -> recipient.status == LogStatus.FAILED
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val filterChips = listOf(
        FilterChipItem("ALL", "Tất cả", stats.totalCount),
        FilterChipItem("PENDING", "Chờ gọi", stats.pendingCount),
        FilterChipItem("SUCCESS", "Thành công", stats.calledCount),
        FilterChipItem("FAILED", "Thất bại", stats.failedCount)
    )

    val genericItems = filteredList.map { recipient ->
        GenericCustomerItemData(
            id = recipient.id,
            name = recipient.name,
            phone = recipient.phone,
            subtitle = if (recipient.callDurationSeconds > 0) "Thời lượng: ${recipient.callDurationSeconds}s" else null,
            status = recipient.status,
            isSelected = recipient.isSelected,
            note = recipient.note
        )
    }

    GenericRecipientListContent(
        totalTitle = "TỔNG SỐ SĐT GỌI",
        totalCount = stats.totalCount,
        successTitle = "ĐÃ GỌI THÀNH CÔNG",
        successCount = stats.calledCount,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        searchPlaceholder = "Tìm tên, SĐT...",
        selectedFilter = selectedFilter,
        onFilterChange = onFilterChange,
        filterChips = filterChips,
        items = genericItems,
        blacklistedPhones = blacklistedPhones,
        onToggleSelection = { id ->
            val recipient = recipients.find { it.id == id }
            if (recipient != null) {
                onToggleSelection(id, recipient.isSelected)
            }
        },
        onSelectAll = onSelectAll,
        onAddRecipient = onAddRecipient,
        onDeleteRecipient = onDeleteRecipient,
        onAddToBlacklist = onAddToBlacklist,
        onImportExcel = onImportExcel,
        addDialogTitle = "Thêm SĐT gọi",
        modifier = modifier,
        emptyStateMessage = "Chưa có danh sách SĐT gọi nào.",
        bottomBar = {
            if (recipients.isNotEmpty()) {
                CampaignBottomActionBar(
                    primaryButtonText = "Bắt đầu tự động gọi (SIM $selectedSimSlot)",
                    primaryButtonIcon = Icons.Default.PhoneInTalk,
                    onPrimaryClick = onStartCampaign,
                    enabled = true
                )
            }
        }
    )
}
