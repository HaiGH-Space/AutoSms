package com.gmail.nthai.presentation.components.config

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.domain.model.UpdateState
import com.gmail.nthai.presentation.components.DownloadProgressDialog
import com.gmail.nthai.presentation.components.DownloadedInstallDialog
import com.gmail.nthai.presentation.components.NoUpdateAvailableDialog
import com.gmail.nthai.presentation.components.UpdateAvailableDialog
import com.gmail.nthai.presentation.components.UpdateErrorDialog
import com.gmail.nthai.presentation.viewmodel.ConfigViewModel
import com.gmail.nthai.presentation.viewmodel.UpdateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleAndUpdateTab(
    viewModel: ConfigViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel(),
    showTopBar: Boolean = false
) {
    val context = LocalContext.current
    val rule by viewModel.sendingRule.collectAsStateWithLifecycle()
    val updateState by updateViewModel.updateState.collectAsStateWithLifecycle()

    when (val state = updateState) {
        is UpdateState.UpdateAvailable -> {
            UpdateAvailableDialog(
                updateInfo = state.updateInfo,
                onUpdateConfirm = {
                    updateViewModel.startDownload(context, state.updateInfo)
                },
                onDismiss = {
                    updateViewModel.dismissState()
                }
            )
        }
        is UpdateState.Downloading -> {
            DownloadProgressDialog(
                progressPercent = state.progressPercent,
                bytesDownloaded = state.bytesDownloaded,
                totalBytes = state.totalBytes,
                onCancel = {
                    updateViewModel.cancelDownload()
                }
            )
        }
        is UpdateState.Downloaded -> {
            DownloadedInstallDialog(
                apkFile = state.apkFile,
                updateInfo = state.updateInfo,
                onInstall = { file ->
                    val installed = updateViewModel.installApk(context, file)
                    if (installed) {
                        updateViewModel.dismissState()
                    }
                },
                onDismiss = {
                    updateViewModel.dismissState()
                }
            )
        }
        is UpdateState.NoUpdateAvailable -> {
            NoUpdateAvailableDialog(
                currentVersionName = state.currentVersionName,
                onDismiss = {
                    updateViewModel.dismissState()
                }
            )
        }
        is UpdateState.Error -> {
            UpdateErrorDialog(
                errorMessage = state.message,
                onDismiss = {
                    updateViewModel.dismissState()
                }
            )
        }
        else -> {}
    }

    var minDelayText by remember(rule) { mutableStateOf(rule.minDelaySec.toString()) }
    var maxDelayText by remember(rule) { mutableStateOf(rule.maxDelaySec.toString()) }
    var startHour by remember(rule) { mutableStateOf(rule.startHour) }
    var startMinute by remember(rule) { mutableStateOf(rule.startMinute) }
    var endHour by remember(rule) { mutableStateOf(rule.endHour) }
    var endMinute by remember(rule) { mutableStateOf(rule.endMinute) }
    var phoneColumnNameText by remember(rule) { mutableStateOf(rule.phoneColumnName) }

    val minDelay = minDelayText.toIntOrNull()
    val maxDelay = maxDelayText.toIntOrNull()
    val startTime = startHour * 60 + startMinute
    val endTime = endHour * 60 + endMinute
    val ruleError = when {
        minDelay == null || maxDelay == null -> "Vui lòng nhập đầy đủ khoảng nghỉ."
        minDelay < 0 -> "Thời gian trễ tối thiểu không được âm."
        maxDelay < minDelay -> "Thời gian trễ tối đa phải lớn hơn hoặc bằng tối thiểu."
        startHour !in 0..23 || startMinute !in 0..59 -> "Giờ bắt đầu không hợp lệ."
        endHour !in 0..24 || endMinute !in 0..59 || endHour == 24 && endMinute != 0 -> "Giờ kết thúc không hợp lệ."
        endTime <= startTime -> "Giờ kết thúc phải sau giờ bắt đầu."
        phoneColumnNameText.isBlank() -> "Tên cột số điện thoại không được để trống."
        else -> null
    }

    val content: @Composable (Modifier) -> Unit = { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Lịch gửi & khoảng nghỉ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.Shuffle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "Khoảng nghỉ ngẫu nhiên",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Ứng dụng chọn thời gian chờ trong khoảng này giữa các tin nhắn.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = minDelayText,
                            onValueChange = { minDelayText = it },
                            label = { Text("Trễ tối thiểu (s)") },
                            isError = minDelay == null || minDelay < 0,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = maxDelayText,
                            onValueChange = { maxDelayText = it },
                            label = { Text("Trễ tối đa (s)") },
                            isError = maxDelay == null || minDelay != null && maxDelay < minDelay,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    FilterChip(
                        selected = minDelayText == "40" && maxDelayText == "50",
                        onClick = {
                            minDelayText = "40"
                            maxDelayText = "50"
                        },
                        label = { Text("Dùng khoảng gợi ý 40–50 giây") }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "Khung giờ gửi trong ngày",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Ngoài khung giờ này, ứng dụng sẽ không gửi tin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        startHour = hour
                                        startMinute = minute
                                    },
                                    startHour,
                                    startMinute,
                                    true
                                ).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Bắt đầu", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    "${startHour.toString().padStart(2, '0')}:${startMinute.toString().padStart(2, '0')}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        if (hour == 0 && minute == 0) {
                                            endHour = 24
                                            endMinute = 0
                                        } else {
                                            endHour = hour
                                            endMinute = minute
                                        }
                                    },
                                    if (endHour == 24) 0 else endHour,
                                    endMinute,
                                    true
                                ).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Kết thúc", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    "${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Khung giờ gửi", style = MaterialTheme.typography.labelLarge)
                            Text(
                                "${startHour.toString().padStart(2, '0')}:${startMinute.toString().padStart(2, '0')} – ${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    AssistChip(
                        onClick = {
                            startHour = 8
                            startMinute = 0
                            endHour = 17
                            endMinute = 0
                        },
                        label = { Text("Áp dụng khung giờ gợi ý 08:00–17:00") }
                    )

                    Text(
                        "Cột số điện thoại trong Excel",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = phoneColumnNameText,
                        onValueChange = { phoneColumnNameText = it },
                        label = { Text("Tên tiêu đề cột SĐT") },
                        supportingText = { Text("Phải khớp với tên cột trong tệp Excel, ví dụ: sdt.") },
                        isError = phoneColumnNameText.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (ruleError == null) {
                                viewModel.updateRule(
                                    minDelay!!,
                                    maxDelay!!,
                                    startHour,
                                    startMinute,
                                    endHour,
                                    endMinute,
                                    phoneColumnNameText
                                )
                                Toast.makeText(context, "Đã lưu cài đặt quy tắc!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = ruleError == null
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Lưu quy tắc gửi")
                    }
                    if (ruleError != null) {
                        Text(
                            ruleError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hệ thống & Cập nhật ứng dụng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "AutoSms v${updateViewModel.getCurrentVersionName()}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Build code: ${updateViewModel.getCurrentVersionCode()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            updateViewModel.checkForUpdate(isManualCheck = true)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = updateState !is UpdateState.Checking
                    ) {
                        if (updateState is UpdateState.Checking) {
                            Text("Đang kiểm tra cập nhật...")
                        } else {
                            Text("Kiểm tra cập nhật ngay")
                        }
                    }
                }
            }
        }
    }

    if (showTopBar) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Quy định & Cập nhật", fontWeight = FontWeight.Bold) }
                )
            }
        ) { innerPadding ->
            content(Modifier.padding(innerPadding))
        }
    } else {
        content(Modifier)
    }
}
