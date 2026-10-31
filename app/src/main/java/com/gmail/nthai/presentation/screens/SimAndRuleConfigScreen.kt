package com.gmail.nthai.presentation.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.domain.model.UpdateState
import com.gmail.nthai.presentation.components.DownloadProgressDialog
import com.gmail.nthai.presentation.components.DownloadedInstallDialog
import com.gmail.nthai.presentation.components.NoUpdateAvailableDialog
import com.gmail.nthai.presentation.components.UpdateAvailableDialog
import com.gmail.nthai.presentation.components.UpdateErrorDialog
import com.gmail.nthai.presentation.viewmodel.ConfigViewModel
import com.gmail.nthai.presentation.viewmodel.UpdateViewModel
import com.gmail.nthai.util.PermissionUtils

@Composable
fun SimManagementTab(
    viewModel: ConfigViewModel = hiltViewModel(),
    showTopBar: Boolean = false
) {
    val context = LocalContext.current
    var hasPhonePermission by remember {
        mutableStateOf(PermissionUtils.hasPhonePermissions(context))
    }
    var hasSmsPermission by remember {
        mutableStateOf(PermissionUtils.hasSmsPermission(context))
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasPhonePermission = PermissionUtils.hasPhonePermissions(context)
        hasSmsPermission = PermissionUtils.hasSmsPermission(context)
        viewModel.loadSims()
    }

    val simConfigs by viewModel.simConfigs.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        if (hasPhonePermission && hasSmsPermission) {
            viewModel.loadSims()
        } else {
            permissionsLauncher.launch(PermissionUtils.getRequiredPermissions())
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPhonePermission = PermissionUtils.hasPhonePermissions(context)
                hasSmsPermission = PermissionUtils.hasSmsPermission(context)
                viewModel.loadSims()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                text = "Quản lý Hạn mức & Trạng thái SIM",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Trạng thái quyền hệ thống:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("• Quyền Điện thoại (đọc SIM): ${if (hasPhonePermission) "Đã cấp" else "Chưa cấp"}")
                    Text("• Quyền Gửi SMS: ${if (hasSmsPermission) "Đã cấp" else "Chưa cấp"}")

                    if (!hasPhonePermission || !hasSmsPermission) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cần đầy đủ quyền Điện thoại & Gửi SMS để ứng dụng nhận diện 2 SIM và phát tin.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    permissionsLauncher.launch(PermissionUtils.getRequiredPermissions())
                                }
                            ) {
                                Text("Cấp quyền ngay")
                            }
                            TextButton(
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", context.packageName, null)
                                        )
                                    )
                                }
                            ) {
                                Text("Mở Cài đặt")
                            }
                        }
                    }
                }
            }

            simConfigs.forEach { config ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SimCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "SIM ${config.simSlot}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val carrierLabel = when {
                            !hasPhonePermission -> "Cần quyền đọc SIM"
                            config.subscriptionId == -1 -> "Chưa phát hiện SIM"
                            config.carrierName.isBlank() -> "Chưa xác định nhà mạng"
                            else -> config.carrierName
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = carrierLabel,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Đã gửi hôm nay: ${config.sentCountToday} tin")

                        Spacer(modifier = Modifier.height(8.dp))

                        var limitText by remember(config.dailyLimit) { mutableStateOf(config.dailyLimit.toString()) }

                        OutlinedTextField(
                            value = limitText,
                            onValueChange = {
                                limitText = it
                                val limit = it.toIntOrNull() ?: 100
                                viewModel.updateSimLimit(config.simSlot, limit)
                            },
                            label = { Text("Giới hạn tin / ngày (Mặc định 100)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }

    if (showTopBar) {
        @OptIn(ExperimentalMaterial3Api::class)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Quản lý SIM", fontWeight = FontWeight.Bold) }
                )
            }
        ) { innerPadding ->
            content(Modifier.padding(innerPadding))
        }
    } else {
        content(Modifier)
    }
}

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
    var startHourText by remember(rule) { mutableStateOf(rule.startHour.toString()) }
    var endHourText by remember(rule) { mutableStateOf(rule.endHour.toString()) }
    var phoneColumnNameText by remember(rule) { mutableStateOf(rule.phoneColumnName) }

    val content: @Composable (Modifier) -> Unit = { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quy tắc Gửi tin & Khung giờ",
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
                    Text("Khoảng trễ ngẫu nhiên giữa 2 tin nhắn (giây):")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = minDelayText,
                            onValueChange = { minDelayText = it },
                            label = { Text("Trễ tối thiểu (s)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = maxDelayText,
                            onValueChange = { maxDelayText = it },
                            label = { Text("Trễ tối đa (s)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Khung giờ cho phép gửi tin nhắn trong ngày (Giờ):")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = startHourText,
                            onValueChange = { startHourText = it },
                            label = { Text("Giờ bắt đầu (VD: 8)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = endHourText,
                            onValueChange = { endHourText = it },
                            label = { Text("Giờ kết thúc (VD: 17)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Tên cột Số điện thoại trong file Excel:")

                    OutlinedTextField(
                        value = phoneColumnNameText,
                        onValueChange = { phoneColumnNameText = it },
                        label = { Text("Tên tiêu đề cột SĐT (Mặc định: sdt)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val min = minDelayText.toIntOrNull() ?: 10
                            val max = maxDelayText.toIntOrNull() ?: 30
                            val start = startHourText.toIntOrNull() ?: 8
                            val end = endHourText.toIntOrNull() ?: 17

                            viewModel.updateRule(min, max, start, end, phoneColumnNameText)
                            Toast.makeText(context, "Đã lưu cài đặt quy tắc!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Lưu quy tắc gửi")
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

// Backward compatibility wrapper if needed
@Composable
fun SimAndRuleConfigScreen(
    viewModel: ConfigViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel(),
    showTopBar: Boolean = true
) {
    updateViewModel.getCurrentVersionName()
    SimManagementTab(viewModel = viewModel, showTopBar = showTopBar)
}
