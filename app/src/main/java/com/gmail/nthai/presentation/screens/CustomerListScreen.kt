package com.gmail.nthai.presentation.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.presentation.components.LoadingDialog
import com.gmail.nthai.presentation.components.SelectPhoneColumnDialog
import com.gmail.nthai.presentation.components.customer.CustomerDetailDialog
import com.gmail.nthai.presentation.components.customer.CustomerItemCard
import com.gmail.nthai.presentation.viewmodel.CustomerCarrierFilter
import com.gmail.nthai.presentation.viewmodel.CustomerFilter
import com.gmail.nthai.presentation.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    simSlot: Int,
    onBack: () -> Unit,
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
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Toast.makeText(context, "Không thể mở file Excel đã chọn.", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.prepareExcelImport(inputStream) { err ->
                        Toast.makeText(context, "Lỗi đọc tiêu đề Excel: ${err.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Khởi tạo file thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(simSlot) {
        viewModel.selectSimSlot(simSlot)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại trang chủ"
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            "Khách hàng",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (simCarrierName != null) "Danh sách gửi bằng SIM $simSlot ($simCarrierName)" else "Danh sách gửi bằng SIM $simSlot",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (customers.isNotEmpty()) {
                        IconButton(onClick = { viewModel.deleteAllCustomers() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa tất cả")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                }
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = "Nhập Excel")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
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
                                "TỔNG KHÁCH HÀNG",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                customers.size.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "ĐÃ CHỌN GỬI",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "$selectedCustomerCount khách",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tìm theo tên, SĐT, mã HĐ...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == CustomerFilter.ALL,
                    onClick = { viewModel.updateFilter(CustomerFilter.ALL) },
                    label = { Text("Tất cả") }
                )
                FilterChip(
                    selected = selectedFilter == CustomerFilter.PENDING,
                    onClick = { viewModel.updateFilter(CustomerFilter.PENDING) },
                    label = { Text("Chưa gửi") }
                )
                FilterChip(
                    selected = selectedFilter == CustomerFilter.SENT,
                    onClick = { viewModel.updateFilter(CustomerFilter.SENT) },
                    label = { Text("Thành công") }
                )
                FilterChip(
                    selected = selectedFilter == CustomerFilter.FAILED,
                    onClick = { viewModel.updateFilter(CustomerFilter.FAILED) },
                    label = { Text("Lỗi") }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CustomerCarrierFilter.entries.forEach { carrierFilter ->
                    FilterChip(
                        selected = selectedCarrierFilter == carrierFilter,
                        onClick = { viewModel.updateCarrierFilter(carrierFilter) },
                        label = { Text(carrierFilter.label) }
                    )
                }
            }

            if (customers.isNotEmpty()) {
                val allSelected = customers.all { it.isSelected }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.selectAll(!allSelected) }
                    ) {
                        Checkbox(
                            checked = allSelected,
                            onCheckedChange = { viewModel.selectAll(it) }
                        )
                        Text(
                            if (allSelected) "Bỏ chọn tất cả" else "Chọn tất cả",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = if (simCarrierName != null) {
                            "Đã chọn (SIM $selectedSimSlot - $simCarrierName): $selectedCustomerCount"
                        } else {
                            "Đã chọn (SIM $selectedSimSlot): $selectedCustomerCount"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có dữ liệu khách hàng.\nBấm nút 'Nhập Excel' để thêm danh sách.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(customers, key = { it.id }) { customer ->
                        val onToggle = remember(customer.id) { { viewModel.toggleCustomerSelection(customer) } }
                        val onDelete = remember(customer.id) { { viewModel.deleteCustomer(customer) } }
                        val onClick = remember(customer.id) { { selectedCustomer = customer } }

                        CustomerItemCard(
                            customer = customer,
                            onToggleSelection = onToggle,
                            onDelete = onDelete,
                            onClick = onClick
                        )
                    }
                }
            }
        }
    }

    pendingImportState?.let { state ->
        SelectPhoneColumnDialog(
            headers = state.headers,
            selectedHeader = state.selectedHeader,
            onSelectHeader = { viewModel.updateSelectedImportHeader(it) },
            onConfirm = {
                viewModel.confirmImport { result ->
                    result.onSuccess { count ->
                        Toast.makeText(context, "Đã nhập $count khách hàng", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        Toast.makeText(context, "Lỗi đọc file Excel: ${err.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDismiss = {
                viewModel.cancelImport()
            }
        )
    }

    selectedCustomer?.let { customer ->
        CustomerDetailDialog(
            customer = customer,
            onDismiss = { selectedCustomer = null }
        )
    }

    if (isImporting) {
        LoadingDialog(message = importLoadingMessage)
    }
}
