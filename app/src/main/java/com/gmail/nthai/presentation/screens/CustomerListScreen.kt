package com.gmail.nthai.presentation.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.presentation.components.ExcelImportDialogs
import com.gmail.nthai.presentation.components.common.AutoSmsSearchBar
import com.gmail.nthai.presentation.components.common.AutoSmsTopAppBar
import com.gmail.nthai.presentation.components.customer.CustomerBottomSendingBar
import com.gmail.nthai.presentation.components.customer.CustomerDetailDialog
import com.gmail.nthai.presentation.components.customer.CustomerEmptyState
import com.gmail.nthai.presentation.components.customer.CustomerFilterSection
import com.gmail.nthai.presentation.components.customer.CustomerItemCard
import com.gmail.nthai.presentation.components.customer.CustomerSelectionBar
import com.gmail.nthai.presentation.components.customer.ResetStatusDialog
import com.gmail.nthai.presentation.components.customer.SimSlotTabs
import com.gmail.nthai.presentation.viewmodel.CustomerViewModel
import com.gmail.nthai.util.PhoneNumberUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    simSlot: Int = 1,
    showTopBar: Boolean = true,
    onBack: (() -> Unit)? = null,
    onStartSending: (() -> Unit)? = null,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val customers by viewModel.customersState.collectAsStateWithLifecycle()
    val blacklistedPhones by viewModel.blacklistedPhones.collectAsStateWithLifecycle()
    val searchQuery by viewModel.query.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.filter.collectAsStateWithLifecycle()
    val selectedCarrierFilter by viewModel.carrierFilter.collectAsStateWithLifecycle()
    val selectedSimSlot by viewModel.simSlot.collectAsStateWithLifecycle()
    val simConfigs by viewModel.simConfigs.collectAsStateWithLifecycle()
    val currentSimConfig = simConfigs.find { it.simSlot == selectedSimSlot }
    val simCarrierName = currentSimConfig?.carrierName?.trim()?.takeIf { it.isNotEmpty() }
    val selectedCustomerCount by viewModel.selectedCustomerCount.collectAsStateWithLifecycle()
    val pendingImportState by viewModel.pendingImportState.collectAsStateWithLifecycle()
    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()
    val importLoadingMessage by viewModel.importLoadingMessage.collectAsStateWithLifecycle()
    val isSending by viewModel.isSending.collectAsStateWithLifecycle()
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.prepareExcelImportWithUri(context, uri) { err ->
                Toast.makeText(context, "Lỗi đọc tiêu đề Excel: ${err.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    LaunchedEffect(simSlot) {
        viewModel.selectSimSlot(simSlot)
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                AutoSmsTopAppBar(
                    title = "Khách hàng",
                    subtitle = if (simCarrierName != null) "SIM $selectedSimSlot · $simCarrierName" else "SIM $selectedSimSlot",
                    navigationIcon = if (onBack != null) {
                        {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Quay lại",
                                    tint = Color(0xFF171717)
                                )
                            }
                        }
                    } else null,
                    actions = {
                        IconButton(
                            onClick = {
                                filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                            }
                        ) {
                            Icon(
                                Icons.Default.FileUpload,
                                contentDescription = "Nhập Excel",
                                tint = Color(0xFF171717)
                            )
                        }
                        if (customers.isNotEmpty()) {
                            IconButton(onClick = { showResetDialog = true }, enabled = !isSending) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Đặt lại trạng thái",
                                    tint = Color(0xFF888888)
                                )
                            }
                            IconButton(onClick = { viewModel.deleteAllCustomers() }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Xóa tất cả",
                                    tint = Color(0xFFEE0000)
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (onStartSending != null) {
                CustomerBottomSendingBar(
                    selectedCount = selectedCustomerCount,
                    simSlot = selectedSimSlot,
                    onStartSending = { onStartSending.invoke() }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFAFAFA))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // SIM Slot Switcher Tabs
            SimSlotTabs(
                selectedSimSlot = selectedSimSlot,
                simConfigs = simConfigs,
                onSelectSimSlot = { viewModel.selectSimSlot(it) }
            )

            // Search Bar
            AutoSmsSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Tìm theo tên, số điện thoại, mã HĐ..."
            )

            // Filters Section
            CustomerFilterSection(
                selectedFilter = selectedFilter,
                selectedCarrierFilter = selectedCarrierFilter,
                onFilterChange = { viewModel.updateFilter(it) },
                onCarrierFilterChange = { viewModel.updateCarrierFilter(it) }
            )

            // Select All & Counters Bar
            if (customers.isNotEmpty()) {
                val allSelected = customers.all { it.isSelected }
                CustomerSelectionBar(
                    allSelected = allSelected,
                    selectedCount = selectedCustomerCount,
                    totalCount = customers.size,
                    onSelectAllChange = { viewModel.selectAll(it) }
                )
            }

            // Customer List or Empty State
            if (customers.isEmpty()) {
                CustomerEmptyState(
                    onImportClick = {
                        filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(customers, key = { it.id }) { customer ->
                        val normalized = PhoneNumberUtils.normalizePhoneNumber(customer.phone)
                        val isBlacklisted = blacklistedPhones.contains(normalized)
                        CustomerItemCard(
                            customer = customer,
                            onToggleSelection = { viewModel.toggleCustomerSelection(customer) },
                            onDelete = { viewModel.deleteCustomer(customer) },
                            onClick = { selectedCustomer = customer },
                            isBlacklisted = isBlacklisted,
                            onAddToBlacklist = { viewModel.addToBlacklist(customer.phone, "Thêm từ danh sách khách hàng") }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }

    selectedCustomer?.let { customer ->
        val normalized = PhoneNumberUtils.normalizePhoneNumber(customer.phone)
        val isBlacklisted = blacklistedPhones.contains(normalized)
        CustomerDetailDialog(
            customer = customer,
            isBlacklisted = isBlacklisted,
            onAddToBlacklist = { viewModel.addToBlacklist(customer.phone, "Thêm từ danh sách khách hàng") },
            onDismiss = { selectedCustomer = null }
        )
    }

    ExcelImportDialogs(
        isImporting = isImporting,
        importLoadingMessage = importLoadingMessage,
        pendingImportState = pendingImportState,
        onSelectHeader = { viewModel.updateSelectedImportHeader(it) },
        onSelectImportMode = { viewModel.updateSelectedImportMode(it) },
        onConfirmImport = {
            viewModel.confirmImport { result ->
                result.onSuccess { count ->
                    Toast.makeText(
                        context,
                        "Đã nhập thành công $count khách hàng",
                        Toast.LENGTH_SHORT
                    ).show()
                }.onFailure { err ->
                    Toast.makeText(
                        context,
                        "Lỗi đọc file Excel: ${err.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        },
        onDismissImport = { viewModel.cancelImport() }
    )

    if (showResetDialog) {
        ResetStatusDialog(
            onConfirm = { scope ->
                viewModel.resetCustomerStatus(scope) {
                    Toast.makeText(
                        context,
                        "Đã đặt lại trạng thái về 'Chờ gửi'",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }
}
