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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.presentation.components.common.LogRecordCard
import com.gmail.nthai.presentation.components.report.DateRangePickerDialog
import com.gmail.nthai.presentation.components.report.ReportMetricHeader
import com.gmail.nthai.presentation.viewmodel.ReportStats
import com.gmail.nthai.presentation.viewmodel.ReportViewModel
import com.gmail.nthai.util.DateTimeUtils
import kotlinx.coroutines.launch
import java.io.IOException

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
        val start = rangeStartMillis?.let(DateTimeUtils::localStartOfDay)
        val endExclusive = rangeEndMillis?.let(DateTimeUtils::localStartOfNextDay)
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
            "${DateTimeUtils.formatPickerDate(rangeStartMillis!!)} – ${DateTimeUtils.formatPickerDate(rangeEndMillis!!)}"
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
                ReportMetricHeader(stats = stats)
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
                    LogRecordCard(log = log)
                }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = rangeStartMillis,
            initialSelectedEndDateMillis = rangeEndMillis
        )
        DateRangePickerDialog(
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
