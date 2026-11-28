package com.gmail.nthai.presentation.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gmail.nthai.presentation.components.customer.CustomerEmptyState
import com.gmail.nthai.presentation.components.customer.CustomerSelectionBar

@Composable
fun GenericRecipientListContent(
    totalTitle: String = "TỔNG SỐ SĐT",
    totalCount: Int,
    successTitle: String = "ĐÃ THÀNH CÔNG",
    successCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchPlaceholder: String = "Tìm tên, SĐT...",
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    filterChips: List<FilterChipItem>,
    items: List<GenericCustomerItemData>,
    modifier: Modifier = Modifier,
    blacklistedPhones: Set<String> = emptySet(),
    onToggleSelection: ((id: Long) -> Unit)? = null,
    onSelectAll: ((Boolean) -> Unit)? = null,
    onAddRecipient: (name: String, phone: String) -> Unit,
    onDeleteRecipient: (id: Long) -> Unit,
    onAddToBlacklist: (phone: String) -> Unit,
    onRetryRecipient: ((id: Long) -> Unit)? = null,
    onImportExcel: () -> Unit,
    addDialogTitle: String = "Thêm người nhận",
    emptyStateMessage: String = "Chưa có danh sách số điện thoại nào.",
    bottomBar: (@Composable () -> Unit)? = null
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header Summary Card
        CustomerHeaderSummary(
            totalTitle = totalTitle,
            totalCount = totalCount,
            successTitle = successTitle,
            successCount = successCount
        )

        // 2. Search Bar & Action Buttons
        CustomerSearchBarWithActions(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = searchPlaceholder,
            onAddClick = { showAddDialog = true },
            onImportExcelClick = onImportExcel
        )

        // 3. Filter Chips
        if (filterChips.isNotEmpty()) {
            GenericFilterChipsBar(
                chips = filterChips,
                selectedKey = selectedFilter,
                onSelectKey = onFilterChange
            )
        }

        // 4. Selection Bar (Select All / Unselect All) if supported
        if (onSelectAll != null && items.isNotEmpty()) {
            val selectedCount = items.count { it.isSelected }
            val allSelected = items.isNotEmpty() && items.all { it.isSelected }
            CustomerSelectionBar(
                allSelected = allSelected,
                selectedCount = selectedCount,
                totalCount = items.size,
                onSelectAllChange = onSelectAll
            )
        }

        // 5. Item List / Empty State
        if (items.isEmpty()) {
            CustomerEmptyState(
                onImportClick = onImportExcel,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = items,
                    key = { it.id }
                ) { itemData ->
                    val isBlacklisted = blacklistedPhones.contains(itemData.phone)
                    GenericCustomerItemCard(
                        item = itemData,
                        onToggleSelection = if (onToggleSelection != null) {
                            { onToggleSelection(itemData.id) }
                        } else null,
                        onDelete = { onDeleteRecipient(itemData.id) },
                        isBlacklisted = isBlacklisted,
                        onAddToBlacklist = { onAddToBlacklist(itemData.phone) }
                    )
                }
            }
        }

        // 6. Optional Bottom Bar slot
        bottomBar?.invoke()
    }

    if (showAddDialog) {
        AddCustomerDialog(
            onDismiss = { showAddDialog = false },
            onAddCustomer = onAddRecipient,
            title = addDialogTitle
        )
    }
}
