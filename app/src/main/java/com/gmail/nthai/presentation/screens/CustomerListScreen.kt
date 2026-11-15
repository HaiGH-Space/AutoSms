package com.gmail.nthai.presentation.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.presentation.components.LoadingDialog
import com.gmail.nthai.presentation.components.SelectPhoneColumnDialog
import com.gmail.nthai.presentation.components.common.AutoSmsSearchBar
import com.gmail.nthai.presentation.components.customer.CustomerDetailDialog
import com.gmail.nthai.presentation.components.customer.CustomerItemCard
import com.gmail.nthai.presentation.components.customer.ResetStatusDialog
import com.gmail.nthai.presentation.viewmodel.CustomerCarrierFilter
import com.gmail.nthai.presentation.viewmodel.CustomerFilter
import com.gmail.nthai.presentation.viewmodel.CustomerViewModel

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
                TopAppBar(
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Quay lại",
                                    tint = Color(0xFF171717)
                                )
                            }
                        }
                    },
                    title = {
                        Column {
                            Text(
                                "Khách hàng",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF171717)
                            )
                            Text(
                                if (simCarrierName != null) "SIM $selectedSimSlot · $simCarrierName" else "SIM $selectedSimSlot",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp
                                ),
                                color = Color(0xFF888888)
                            )
                        }
                    },
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFFFFFF)
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selectedCustomerCount > 0 && onStartSending != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    color = Color(0xFFFFFFFF),
                    border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ĐÃ CHỌN GỬI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF888888)
                            )
                            Text(
                                text = "$selectedCustomerCount liên hệ (SIM $selectedSimSlot)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF171717)
                            )
                        }
                        Button(
                            onClick = { onStartSending?.invoke() },
                            shape = RoundedCornerShape(9999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF171717),
                                contentColor = Color(0xFFFFFFFF)
                            ),
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bắt đầu gửi", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
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
            SecondaryTabRow(
                selectedTabIndex = selectedSimSlot - 1,
                containerColor = Color(0xFFFFFFFF),
                contentColor = Color(0xFF171717),
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(selectedSimSlot - 1),
                        color = Color(0xFF171717)
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .border(1.dp, Color(0xFFEBEBEB), RoundedCornerShape(9999.dp))
            ) {
                (1..2).forEach { slot ->
                    val config = simConfigs.find { it.simSlot == slot }
                    val label =
                        if (!config?.carrierName.isNullOrBlank()) "SIM $slot (${config?.carrierName})" else "SIM $slot"
                    Tab(
                        selected = selectedSimSlot == slot,
                        onClick = { viewModel.selectSimSlot(slot) },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.SimCard,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (selectedSimSlot == slot) Color(0xFF171717) else Color(
                                        0xFF888888
                                    )
                                )
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedSimSlot == slot) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedSimSlot == slot) Color(0xFF171717) else Color(
                                        0xFF888888
                                    )
                                )
                            }
                        }
                    )
                }
            }

            // Search Bar
            AutoSmsSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Tìm theo tên, số điện thoại, mã HĐ..."
            )

            // Filters Section (2 separate rows with tight vertical spacing)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Status Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VercelFilterPill(
                        selected = selectedFilter == CustomerFilter.ALL,
                        text = "Tất cả",
                        onClick = { viewModel.updateFilter(CustomerFilter.ALL) }
                    )
                    VercelFilterPill(
                        selected = selectedFilter == CustomerFilter.PENDING,
                        text = "Chờ gửi",
                        onClick = { viewModel.updateFilter(CustomerFilter.PENDING) }
                    )
                    VercelFilterPill(
                        selected = selectedFilter == CustomerFilter.SENT,
                        text = "Thành công",
                        onClick = { viewModel.updateFilter(CustomerFilter.SENT) }
                    )
                    VercelFilterPill(
                        selected = selectedFilter == CustomerFilter.FAILED,
                        text = "Lỗi",
                        onClick = { viewModel.updateFilter(CustomerFilter.FAILED) }
                    )
                }

                // Carrier Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CustomerCarrierFilter.entries.forEach { carrierFilter ->
                        VercelFilterPill(
                            selected = selectedCarrierFilter == carrierFilter,
                            text = carrierFilter.label,
                            onClick = { viewModel.updateCarrierFilter(carrierFilter) }
                        )
                    }
                }
            }

            // Select All & Counters Bar
            if (customers.isNotEmpty()) {
                val allSelected = customers.all { it.isSelected }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.selectAll(!allSelected) }
                    ) {
                        Checkbox(
                            checked = allSelected,
                            onCheckedChange = { viewModel.selectAll(it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF171717),
                                checkmarkColor = Color(0xFFFFFFFF),
                                uncheckedColor = Color(0xFF888888)
                            )
                        )
                        Text(
                            text = if (allSelected) "Bỏ chọn tất cả" else "Chọn tất cả",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF171717)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0xFFFFFFFF),
                        border = BorderStroke(1.dp, Color(0xFFEBEBEB))
                    ) {
                        Text(
                            text = "Đã chọn: $selectedCustomerCount / ${customers.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF171717),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Customer List or Empty State
            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFFFFF),
                        border = BorderStroke(1.dp, Color(0xFFEBEBEB))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "CHƯA CÓ LIÊN HỆ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF888888)
                            )
                            Text(
                                text = "Nạp danh sách từ file Excel (.xlsx, .xls) để phân bổ gửi tin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF666666),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                },
                                shape = RoundedCornerShape(9999.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF171717),
                                    contentColor = Color(0xFFFFFFFF)
                                )
                            ) {
                                Icon(
                                    Icons.Default.FileUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Nhập file Excel", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(customers, key = { it.id }) { customer ->
                        CustomerItemCard(
                            customer = customer,
                            onToggleSelection = { viewModel.toggleCustomerSelection(customer) },
                            onDelete = { viewModel.deleteCustomer(customer) },
                            onClick = { selectedCustomer = customer }
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
        CustomerDetailDialog(
            customer = customer,
            onDismiss = { selectedCustomer = null }
        )
    }

    pendingImportState?.let { state ->
        SelectPhoneColumnDialog(
            headers = state.headers,
            selectedHeader = state.selectedHeader,
            onSelectHeader = { viewModel.updateSelectedImportHeader(it) },
            selectedImportMode = state.selectedImportMode,
            onSelectImportMode = { viewModel.updateSelectedImportMode(it) },
            onConfirm = {
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
            onDismiss = { viewModel.cancelImport() }
        )
    }

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

    if (isImporting) {
        LoadingDialog(message = importLoadingMessage)
    }
}

@Composable
private fun VercelFilterPill(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9999.dp),
        color = if (selected) Color(0xFF171717) else Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, if (selected) Color(0xFF171717) else Color(0xFFEBEBEB))
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xFFFFFFFF) else Color(0xFF4D4D4D),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
