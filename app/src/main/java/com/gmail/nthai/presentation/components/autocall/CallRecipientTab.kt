package com.gmail.nthai.presentation.components.autocall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gmail.nthai.domain.model.CallRecipient
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.repository.CallRecipientStats
import com.gmail.nthai.presentation.components.common.AddCustomerDialog
import com.gmail.nthai.presentation.components.common.CustomerHeaderSummary
import com.gmail.nthai.presentation.components.common.CustomerSearchBarWithActions
import com.gmail.nthai.presentation.components.common.FilterChipItem
import com.gmail.nthai.presentation.components.common.GenericCustomerItemCard
import com.gmail.nthai.presentation.components.common.GenericCustomerItemData
import com.gmail.nthai.presentation.components.common.GenericFilterChipsBar
import com.gmail.nthai.presentation.components.customer.CustomerEmptyState
import com.gmail.nthai.presentation.components.customer.CustomerSelectionBar
import com.gmail.nthai.util.PhoneNumberUtils

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
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredList = remember(recipients, searchQuery, selectedFilter) {
        recipients.filter { recipient ->
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
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Summary Header Card
        CustomerHeaderSummary(
            totalTitle = "TỔNG SỐ SĐT GỌI",
            totalCount = stats.totalCount,
            successTitle = "ĐÃ GỌI THÀNH CÔNG",
            successCount = stats.calledCount
        )

        // 2. Search Bar & Action Buttons
        CustomerSearchBarWithActions(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = "Tìm tên, SĐT...",
            onAddClick = { showAddDialog = true },
            onImportExcelClick = onImportExcel
        )

        // 3. Filter Chips
        val chips = listOf(
            FilterChipItem("ALL", "Tất cả", stats.totalCount),
            FilterChipItem("PENDING", "Chờ gọi", stats.pendingCount),
            FilterChipItem("SUCCESS", "Thành công", stats.calledCount),
            FilterChipItem("FAILED", "Thất bại", stats.failedCount)
        )
        GenericFilterChipsBar(
            chips = chips,
            selectedKey = selectedFilter,
            onSelectKey = onFilterChange
        )

        // 4. Selection Bar
        if (recipients.isNotEmpty()) {
            val allSelected = recipients.all { it.isSelected }
            CustomerSelectionBar(
                allSelected = allSelected,
                selectedCount = stats.selectedCount,
                totalCount = recipients.size,
                onSelectAllChange = onSelectAll
            )
        }

        // 5. Customer List or Empty State
        if (filteredList.isEmpty()) {
            CustomerEmptyState(
                onImportClick = onImportExcel,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = filteredList,
                    key = { it.id },
                    contentType = { "call_recipient" }
                ) { recipient ->
                    val isBlacklisted = remember(recipient.phone, blacklistedPhones) {
                        val normalized = PhoneNumberUtils.normalizePhoneNumber(recipient.phone)
                        blacklistedPhones.contains(normalized)
                    }

                    GenericCustomerItemCard(
                        item = GenericCustomerItemData(
                            id = recipient.id,
                            name = recipient.name,
                            phone = recipient.phone,
                            subtitle = if (recipient.callDurationSeconds > 0) "${recipient.callDurationSeconds}s" else null,
                            status = recipient.status,
                            isSelected = recipient.isSelected,
                            note = recipient.note.ifBlank { null }
                        ),
                        onToggleSelection = { onToggleSelection(recipient.id, recipient.isSelected) },
                        onDelete = { onDeleteRecipient(recipient.id) },
                        onClick = { onToggleSelection(recipient.id, recipient.isSelected) },
                        isBlacklisted = isBlacklisted,
                        onAddToBlacklist = { onAddToBlacklist(recipient.phone) }
                    )
                }
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomerDialog(
            title = "Thêm SĐT gọi điện",
            onDismiss = { showAddDialog = false },
            onAddCustomer = onAddRecipient
        )
    }
}
