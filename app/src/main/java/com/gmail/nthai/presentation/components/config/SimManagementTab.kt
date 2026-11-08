package com.gmail.nthai.presentation.components.config

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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.gmail.nthai.domain.model.SimConfig
import com.gmail.nthai.presentation.viewmodel.ConfigViewModel
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
                    containerColor = if (hasPhonePermission && hasSmsPermission) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (hasPhonePermission && hasSmsPermission) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                            contentDescription = null
                        )
                        Text(
                            text = if (hasPhonePermission && hasSmsPermission) "Quyền hệ thống đã sẵn sàng" else "Cần hoàn tất quyền hệ thống",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Đọc SIM: ${if (hasPhonePermission) "Đã cấp" else "Chưa cấp"}  ·  Gửi SMS: ${if (hasSmsPermission) "Đã cấp" else "Chưa cấp"}",
                        style = MaterialTheme.typography.bodyMedium
                    )

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
                SimSlotCard(
                    config = config,
                    hasPhonePermission = hasPhonePermission,
                    onSaveLimit = { limit ->
                        viewModel.updateSimLimit(config.simSlot, limit)
                        Toast.makeText(context, "Đã cập nhật hạn mức SIM ${config.simSlot}", Toast.LENGTH_SHORT).show()
                    }
                )
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

@Composable
fun SimSlotCard(
    config: SimConfig,
    hasPhonePermission: Boolean,
    onSaveLimit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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

            Spacer(modifier = Modifier.height(12.dp))

            val safeLimit = config.dailyLimit.coerceAtLeast(1)
            val progress = (config.sentCountToday.toFloat() / safeLimit).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hạn mức hôm nay", style = MaterialTheme.typography.labelLarge)
                Text(
                    "${config.sentCountToday} / ${config.dailyLimit} tin",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = if (progress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            val defaultCarrierLimit = SimConfig.getDefaultDailyLimit(config.carrierName)
            val carrierHintText = if (config.carrierName.isNotBlank()) {
                "Mặc định ${config.carrierName}: $defaultCarrierLimit tin/ngày"
            } else {
                "Khuyến nghị mặc định: $defaultCarrierLimit tin/ngày"
            }

            Text(
                "Điều chỉnh hạn mức",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            var limitText by remember(config.dailyLimit) { mutableStateOf(config.dailyLimit.toString()) }
            val parsedLimit = limitText.toIntOrNull()
            val isLimitValid = parsedLimit != null && parsedLimit > 0

            OutlinedTextField(
                value = limitText,
                onValueChange = { limitText = it.filter(Char::isDigit) },
                label = { Text("Giới hạn tin nhắn mỗi ngày") },
                supportingText = {
                    Text(if (isLimitValid) "Áp dụng cho SIM ${config.simSlot}. $carrierHintText" else "Nhập số lớn hơn 0.")
                },
                isError = !isLimitValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (config.carrierName.isNotBlank() && parsedLimit != defaultCarrierLimit) {
                    OutlinedButton(
                        onClick = { limitText = defaultCarrierLimit.toString() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Đặt theo ${config.carrierName} ($defaultCarrierLimit)")
                    }
                }
                Button(
                    onClick = {
                        parsedLimit?.let { limit -> onSaveLimit(limit) }
                    },
                    enabled = isLimitValid && parsedLimit != config.dailyLimit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Lưu hạn mức")
                }
            }
        }
    }
}
