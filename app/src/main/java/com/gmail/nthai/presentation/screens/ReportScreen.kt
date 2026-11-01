package com.gmail.nthai.presentation.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.presentation.viewmodel.ReportStats
import com.gmail.nthai.presentation.viewmodel.ReportViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    var selectedSimSlot by rememberSaveable { mutableStateOf<Int?>(null) }
    var rangeStartMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var rangeEndMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    val filteredLogs = remember(logs, selectedSimSlot, rangeStartMillis, rangeEndMillis) {
        val start = rangeStartMillis?.let(::localStartOfDay)
        val endExclusive = rangeEndMillis?.let(::localStartOfNextDay)
        logs.filter { log ->
            (selectedSimSlot == null || log.simSlot == selectedSimSlot) &&
                (start == null || log.timestamp >= start) &&
                (endExclusive == null || log.timestamp < endExclusive)
        }
    }
    val stats = remember(filteredLogs) {
        val total = filteredLogs.size
        val success = filteredLogs.count { it.status == LogStatus.SENT_SUCCESS }
        val failed = filteredLogs.count { it.status == LogStatus.FAILED }
        val pending = filteredLogs.count { it.status == LogStatus.PENDING }
        ReportStats(
            total = total,
            success = success,
            failed = failed,
            pending = pending,
            successRatePercentage = if (total > 0) success * 100f / total else 0f
        )
    }
    val dateRangeLabel = remember(rangeStartMillis, rangeEndMillis) {
        if (rangeStartMillis == null || rangeEndMillis == null) {
            "Khoảng ngày"
        } else {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
            "${formatPickerDate(rangeStartMillis!!, formatter)} – ${formatPickerDate(rangeEndMillis!!, formatter)}"
        }
    }

    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = try {
                    val outputStream = context.contentResolver.openOutputStream(uri)
                        ?: throw IOException("Không thể mở tệp đã chọn.")
                    outputStream.use { viewModel.exportExcel(filteredLogs, it) }
                } catch (error: Exception) {
                    Result.failure(error)
                }
                result.onSuccess {
                    Toast.makeText(context, "Đã xuất báo cáo Excel thành công!", Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Toast.makeText(context, "Xuất file thất bại: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Báo cáo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Thống kê và lịch sử gửi tin",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showClearConfirmation = true },
                        enabled = logs.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Xóa toàn bộ lịch sử",
                            tint = if (logs.isNotEmpty()) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Hiệu quả gửi tin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (selectedSimSlot == null && rangeStartMillis == null) {
                            "Tổng hợp trên toàn bộ lịch sử"
                        } else {
                            "Kết quả theo bộ lọc đang chọn"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReportMetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Tổng tin",
                            value = stats.total.toString(),
                            icon = Icons.Default.Sms,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        ReportMetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Thành công",
                            value = stats.success.toString(),
                            icon = Icons.Default.CheckCircle,
                            containerColor = Color(0xFFE0F2E8),
                            contentColor = Color(0xFF145C38)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReportMetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Thất bại",
                            value = stats.failed.toString(),
                            icon = Icons.Default.ErrorOutline,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                        ReportMetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Tỷ lệ thành công",
                            value = "${String.format(Locale.getDefault(), "%.1f", stats.successRatePercentage)}%",
                            icon = Icons.Default.CheckCircle,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    if (stats.pending > 0) {
                        Text(
                            "Còn ${stats.pending} tin đang chờ xử lý",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Xuất báo cáo", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Excel .xlsx · ${filteredLogs.size} bản ghi theo bộ lọc",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                createFileLauncher.launch("BaoCao_AutoSms_${System.currentTimeMillis()}.xlsx")
                            },
                            enabled = filteredLogs.isNotEmpty(),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text("Xuất")
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Lịch sử gửi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${filteredLogs.size} / ${logs.size} bản ghi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (selectedSimSlot != null || rangeStartMillis != null) {
                            TextButton(
                                onClick = {
                                    selectedSimSlot = null
                                    rangeStartMillis = null
                                    rangeEndMillis = null
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Xóa lọc")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedSimSlot == null,
                            onClick = { selectedSimSlot = null },
                            label = { Text("Tất cả SIM") },
                            leadingIcon = if (selectedSimSlot == null) {
                                { Icon(Icons.Default.SimCard, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = selectedSimSlot == 1,
                            onClick = { selectedSimSlot = 1 },
                            label = { Text("SIM 1") }
                        )
                        FilterChip(
                            selected = selectedSimSlot == 2,
                            onClick = { selectedSimSlot = 2 },
                            label = { Text("SIM 2") }
                        )
                        FilterChip(
                            selected = rangeStartMillis != null,
                            onClick = { showDatePicker = true },
                            label = { Text(dateRangeLabel, maxLines = 1) },
                            leadingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        )
                    }
                }
            }

            if (filteredLogs.isEmpty()) {
                item {
                    EmptyReportState(hasAnyLogs = logs.isNotEmpty())
                }
            } else {
                items(filteredLogs, key = { it.id }) { log ->
                    ReportLogCard(log)
                }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = rangeStartMillis,
            initialSelectedEndDateMillis = rangeEndMillis
        )
        DateRangeDialog(
            state = pickerState,
            onDismiss = { showDatePicker = false },
            onApply = {
                rangeStartMillis = pickerState.selectedStartDateMillis
                rangeEndMillis = pickerState.selectedEndDateMillis
                showDatePicker = false
            }
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Xóa toàn bộ lịch sử?") },
            text = { Text("Thao tác này sẽ xóa ${logs.size} bản ghi gửi tin khỏi thiết bị và không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearLogs()
                        showClearConfirmation = false
                    }
                ) {
                    Text("Xóa lịch sử", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun ReportMetricCard(
    modifier: Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(icon, contentDescription = null, tint = contentColor)
            }
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun EmptyReportState(hasAnyLogs: Boolean) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                if (hasAnyLogs) Icons.Default.DateRange else Icons.Default.Sms,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                if (hasAnyLogs) "Không có kết quả phù hợp" else "Chưa có lịch sử gửi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                if (hasAnyLogs) "Thử đổi SIM hoặc khoảng ngày để xem thêm bản ghi."
                else "Kết quả gửi tin sẽ xuất hiện tại đây.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReportLogCard(log: LogRecord) {
    val dateFormat = remember {
        SimpleDateFormat("HH:mm · dd/MM/yyyy", Locale.getDefault())
    }
    val statusStyle = when (log.status) {
        LogStatus.SENT_SUCCESS -> ReportStatusStyle(
            "Thành công",
            Color(0xFF176B45),
            Color(0xFFE0F2E8),
            Icons.Default.CheckCircle
        )
        LogStatus.FAILED -> ReportStatusStyle(
            "Thất bại",
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.errorContainer,
            Icons.Default.ErrorOutline
        )
        LogStatus.PENDING -> ReportStatusStyle(
            "Đang chờ",
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondaryContainer,
            Icons.Default.Schedule
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        log.customerName.firstOrNull()?.uppercase() ?: "?",
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        log.customerName.ifBlank { "Khách hàng" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        log.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(statusStyle.containerColor)
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            statusStyle.icon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = statusStyle.color
                        )
                        Text(
                            statusStyle.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusStyle.color,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        dateFormat.format(Date(log.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                log.messageContent,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        Icons.Default.SimCard,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "SIM ${log.simSlot}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (log.status == LogStatus.FAILED && !log.errorMessage.isNullOrBlank()) {
                Text(
                    "Lý do: ${log.errorMessage}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f))
                        .padding(10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeDialog(
    state: DateRangePickerState,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .height(620.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                DateRangePicker(
                    state = state,
                    modifier = Modifier.weight(1f),
                    showModeToggle = false,
                    title = {
                        Text(
                            "Chọn khoảng ngày",
                            modifier = Modifier.padding(start = 12.dp, top = 8.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    headline = {
                        Text(
                            dateRangeHeadline(state),
                            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    TextButton(
                        onClick = onApply,
                        enabled = state.selectedStartDateMillis != null &&
                            state.selectedEndDateMillis != null
                    ) {
                        Text("Áp dụng")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun dateRangeHeadline(state: DateRangePickerState): String {
    val start = state.selectedStartDateMillis
    val end = state.selectedEndDateMillis
    return if (start == null || end == null) {
        "Chọn ngày bắt đầu và kết thúc"
    } else {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
        "${formatPickerDate(start, formatter)} – ${formatPickerDate(end, formatter)}"
    }
}

private fun formatPickerDate(
    timestamp: Long,
    formatter: DateTimeFormatter
): String = LocalDate.from(Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC)).format(formatter)

private fun localStartOfDay(pickerTimestamp: Long): Long {
    val selectedDate = LocalDate.from(Instant.ofEpochMilli(pickerTimestamp).atZone(ZoneOffset.UTC))
    return selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun localStartOfNextDay(pickerTimestamp: Long): Long {
    val selectedDate = LocalDate.from(Instant.ofEpochMilli(pickerTimestamp).atZone(ZoneOffset.UTC))
    return selectedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private data class ReportStatusStyle(
    val label: String,
    val color: Color,
    val containerColor: Color,
    val icon: ImageVector
)
