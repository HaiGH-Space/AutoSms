package com.gmail.nthai.presentation.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.presentation.components.common.AutoSmsTopAppBar
import com.gmail.nthai.presentation.components.common.LogRecordCard
import com.gmail.nthai.presentation.components.report.DateRangePickerDialog
import com.gmail.nthai.presentation.components.report.ReportMetricHeader
import com.gmail.nthai.presentation.viewmodel.ReportViewModel
import com.gmail.nthai.util.DateTimeUtils
import kotlinx.coroutines.launch
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    showTopBar: Boolean = true,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val filteredLogs by viewModel.filteredLogs.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val selectedSimSlot by viewModel.selectedSimSlot.collectAsStateWithLifecycle()
    val rangeStartMillis by viewModel.rangeStartMillis.collectAsStateWithLifecycle()
    val rangeEndMillis by viewModel.rangeEndMillis.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    val dateRangeLabel = remember(rangeStartMillis, rangeEndMillis) {
        if (rangeStartMillis == null || rangeEndMillis == null) {
            "Khoảng ngày"
        } else {
            "${DateTimeUtils.formatPickerDate(rangeStartMillis!!)} – ${
                DateTimeUtils.formatPickerDate(
                    rangeEndMillis!!
                )
            }"
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
                    Toast.makeText(context, "Đã xuất báo cáo Excel thành công!", Toast.LENGTH_SHORT)
                        .show()
                }.onFailure { error ->
                    Toast.makeText(
                        context,
                        "Xuất file thất bại: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            if (showTopBar) {
                AutoSmsTopAppBar(
                    title = "Báo cáo",
                    subtitle = "Thống kê và lịch sử gửi tin",
                    actions = {
                        IconButton(
                            onClick = { showClearConfirmation = true },
                            enabled = logs.isNotEmpty()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (logs.isNotEmpty()) Color(0xFFFEF2F2) else Color(0xFFF5F5F5))
                                    .border(1.dp, if (logs.isNotEmpty()) Color(0xFFFEE2E2) else Color(0xFFEBEBEB), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Xóa toàn bộ lịch sử",
                                    modifier = Modifier.size(18.dp),
                                    tint = if (logs.isNotEmpty()) Color(0xFFEE0000) else Color(0xFF888888)
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(contentType = "header_title") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Hiệu quả gửi tin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF171717)
                    )
                    Text(
                        if (selectedSimSlot == null && rangeStartMillis == null) {
                            "Tổng hợp trên toàn bộ lịch sử gửi SMS"
                        } else {
                            "Kết quả chi tiết theo bộ lọc đang chọn"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
            }

            item(contentType = "metrics") {
                ReportMetricHeader(stats = stats)
            }

            item(contentType = "export_card") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFEBEBEB))
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF5F5F5))
                                .border(1.dp, Color(0xFFEBEBEB), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = Color(0xFF171717),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Xuất báo cáo Excel",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF171717)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "File .xlsx · ${filteredLogs.size} bản ghi theo bộ lọc",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF666666)
                            )
                        }
                        Button(
                            onClick = {
                                createFileLauncher.launch("BaoCao_AutoSms_${System.currentTimeMillis()}.xlsx")
                            },
                            enabled = filteredLogs.isNotEmpty(),
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF171717),
                                contentColor = Color(0xFFFFFFFF),
                                disabledContainerColor = Color(0xFFEBEBEB),
                                disabledContentColor = Color(0xFFA1A1A1)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Xuất file", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item(contentType = "filters") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Stable header row with zero layout shift
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Lịch sử gửi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF171717)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color(0xFFF5F5F5))
                                    .border(1.dp, Color(0xFFEBEBEB), RoundedCornerShape(100.dp))
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${filteredLogs.size} / ${logs.size} bản ghi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color(0xFF4D4D4D)
                                )
                            }
                        }
                    }

                    // Sleek horizontal compact pill filter chips with zero jumping
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReportPillFilterChip(
                            text = "Tất cả SIM",
                            selected = selectedSimSlot == null,
                            onClick = { viewModel.setSelectedSimSlot(null) },
                            leadingIcon = if (selectedSimSlot == null) {
                                {
                                    Icon(
                                        Icons.Default.SimCard,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFFFFFFFF)
                                    )
                                }
                            } else null
                        )
                        ReportPillFilterChip(
                            text = "SIM 1",
                            selected = selectedSimSlot == 1,
                            onClick = { viewModel.setSelectedSimSlot(1) }
                        )
                        ReportPillFilterChip(
                            text = "SIM 2",
                            selected = selectedSimSlot == 2,
                            onClick = { viewModel.setSelectedSimSlot(2) }
                        )
                        ReportPillFilterChip(
                            text = dateRangeLabel,
                            selected = rangeStartMillis != null,
                            onClick = { showDatePicker = true },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (rangeStartMillis != null) Color(0xFFFFFFFF) else Color(0xFF666666)
                                )
                            }
                        )

                        if (selectedSimSlot != null || rangeStartMillis != null) {
                            ReportPillFilterChip(
                                text = "Xóa lọc",
                                selected = false,
                                onClick = { viewModel.clearFilters() },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFFEE0000)
                                    )
                                },
                                textColor = Color(0xFFEE0000),
                                borderColor = Color(0xFFF7D4D6),
                                containerColor = Color(0xFFFEF2F2)
                            )
                        }
                    }
                }
            }

            if (filteredLogs.isEmpty()) {
                item(contentType = "empty_state") {
                    EmptyReportState(
                        hasAnyLogs = logs.isNotEmpty(),
                        onClearFilters = { viewModel.clearFilters() }
                    )
                }
            } else {
                items(
                    items = filteredLogs,
                    key = { it.id },
                    contentType = { "log_item" }
                ) { log ->
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
                viewModel.setDateRange(
                    pickerState.selectedStartDateMillis,
                    pickerState.selectedEndDateMillis
                )
                showDatePicker = false
            }
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color(0xFFFFFFFF),
            title = {
                Text(
                    "Xóa toàn bộ lịch sử?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717)
                )
            },
            text = {
                Text(
                    "Thao tác này sẽ xóa ${logs.size} bản ghi gửi tin khỏi thiết bị và không thể hoàn tác.",
                    color = Color(0xFF4D4D4D)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearLogs()
                        showClearConfirmation = false
                    },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEE0000),
                        contentColor = Color(0xFFFFFFFF)
                    )
                ) {
                    Text("Xóa lịch sử", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearConfirmation = false },
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF171717)
                    )
                ) {
                    Text("Hủy", fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

@Composable
fun ReportPillFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    textColor: Color = if (selected) Color(0xFFFFFFFF) else Color(0xFF4D4D4D),
    borderColor: Color = if (selected) Color(0xFF171717) else Color(0xFFEBEBEB),
    containerColor: Color = if (selected) Color(0xFF171717) else Color(0xFFFFFFFF)
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (leadingIcon != null) {
                leadingIcon()
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EmptyReportState(
    hasAnyLogs: Boolean,
    onClearFilters: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFAFAFA),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEBEBEB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
                    .border(1.dp, Color(0xFFEBEBEB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (hasAnyLogs) Icons.Default.FilterList else Icons.Default.Sms,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF171717)
                )
            }
            Text(
                if (hasAnyLogs) "Không có kết quả phù hợp" else "Chưa có lịch sử gửi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF171717)
            )
            Text(
                if (hasAnyLogs) "Thử đổi SIM hoặc chọn khoảng ngày khác để xem thêm bản ghi."
                else "Các kết quả gửi tin SMS tự động sẽ xuất hiện tại đây.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666)
            )
            if (hasAnyLogs) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onClearFilters,
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF171717)
                    )
                ) {
                    Text("Xóa bộ lọc", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
