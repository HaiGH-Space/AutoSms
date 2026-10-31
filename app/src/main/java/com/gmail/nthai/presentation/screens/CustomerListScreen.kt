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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import com.gmail.nthai.domain.usecase.DetectVietnameseCarrierUseCase
import com.gmail.nthai.presentation.viewmodel.CustomerCarrierFilter
import com.gmail.nthai.presentation.viewmodel.CustomerFilter
import com.gmail.nthai.presentation.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    simSlot: Int,
    openImportOnStart: Boolean = false,
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
    var hasLaunchedInitialImport by rememberSaveable { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Toast.makeText(context, "Không thể mở file Excel đã chọn.", Toast.LENGTH_LONG).show()
                    if (openImportOnStart) {
                        onBack()
                    }
                } else {
                    viewModel.prepareExcelImport(inputStream) { err ->
                        Toast.makeText(context, "Lỗi đọc tiêu đề Excel: ${err.message}", Toast.LENGTH_LONG).show()
                        if (openImportOnStart) {
                            onBack()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Khởi tạo file thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                if (openImportOnStart) {
                    onBack()
                }
            }
        } else {
            if (openImportOnStart) {
                onBack()
            }
        }
    }

    LaunchedEffect(simSlot) {
        viewModel.selectSimSlot(simSlot)
    }
    LaunchedEffect(openImportOnStart) {
        if (openImportOnStart && !hasLaunchedInitialImport) {
            hasLaunchedInitialImport = true
            filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        }
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
                if (openImportOnStart) {
                    onBack()
                }
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

@Composable
private fun LoadingDialog(message: String) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Vui lòng đợi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = message.ifBlank { "Đang xử lý file Excel..." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectPhoneColumnDialog(
    headers: List<String>,
    selectedHeader: String,
    onSelectHeader: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn cột Số điện thoại") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 350.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Vui lòng chọn tiêu đề cột trong file Excel chứa danh sách số điện thoại cần gửi tin:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                headers.forEach { header ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectHeader(header) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (header == selectedHeader),
                            onClick = { onSelectHeader(header) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = header,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (header == selectedHeader) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Xác nhận nhập")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun CustomerItemCard(
    customer: Customer,
    onToggleSelection: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = customer.isSelected,
                onCheckedChange = { onToggleSelection() }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusBadge(customer.status)
                    CarrierEstimateBadge(customer.phone)
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (customer.customFields.isEmpty()) {
                    Text(
                        text = "SĐT: ${customer.phone}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    customer.customFields.forEach { (label, value) ->
                        Text(
                            text = "$label: ${value.ifBlank { "—" }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun CustomerDetailDialog(
    customer: Customer,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Dữ liệu khách hàng từ Excel") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (customer.customFields.isEmpty()) {
                    Text(
                        text = "SĐT: ${customer.phone}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Các cột trong file Excel",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    customer.customFields.forEach { (label, value) ->
                        CustomerDetailRow(label, value.ifBlank { "—" })
                    }
                }
                CarrierEstimateBadge(customer.phone)
                Text(
                    text = "Nhà mạng được dự đoán từ đầu số; có thể không chính xác nếu số đã chuyển mạng giữ số.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
private fun CarrierEstimateBadge(phone: String) {
    val carrierName = DetectVietnameseCarrierUseCase.detect(phone)
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = carrierName?.let { it } ?: "Không rõ nhà mạng",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CustomerDetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun StatusBadge(status: CustomerStatus) {
    val (bgColor, textColor, text) = when (status) {
        CustomerStatus.PENDING -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Chờ gửi"
        )
        CustomerStatus.SENT -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Thành công"
        )
        CustomerStatus.FAILED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Lỗi"
        )
    }

    Surface(
        color = bgColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontWeight = FontWeight.Bold
        )
    }
}
