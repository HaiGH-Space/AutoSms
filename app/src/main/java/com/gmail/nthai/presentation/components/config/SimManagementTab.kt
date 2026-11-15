package com.gmail.nthai.presentation.components.config

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                text = "HẠN MỨC & TRẠNG THÁI SIM",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF888888)
            )

            // System Permission Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFFFFF),
                border = BorderStroke(1.dp, if (hasPhonePermission && hasSmsPermission) Color(0xFFEBEBEB) else Color(0xFFF7D4D6))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(
                                    if (hasPhonePermission && hasSmsPermission) Color(0xFF10B981) else Color(0xFFEE0000),
                                    CircleShape
                                )
                        )
                        Text(
                            text = if (hasPhonePermission && hasSmsPermission) "Quyền hệ thống đã sẵn sàng" else "Cần hoàn tất quyền hệ thống",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF171717)
                        )
                    }
                    Text(
                        "Đọc SIM: ${if (hasPhonePermission) "Đã cấp" else "Chưa cấp"}  ·  Gửi SMS: ${if (hasSmsPermission) "Đã cấp" else "Chưa cấp"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = Color(0xFF666666)
                    )

                    if (!hasPhonePermission || !hasSmsPermission) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cần đầy đủ quyền Điện thoại & Gửi SMS để ứng dụng nhận diện 2 SIM và phát tin.",
                            color = Color(0xFFEE0000),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    permissionsLauncher.launch(PermissionUtils.getRequiredPermissions())
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF171717),
                                    contentColor = Color(0xFFFFFFFF)
                                )
                            ) {
                                Text("Cấp quyền ngay", fontWeight = FontWeight.SemiBold)
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
                                Text("Mở Cài đặt", color = Color(0xFF171717))
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
                    title = { Text("Quản lý SIM", fontWeight = FontWeight.Bold, color = Color(0xFF171717)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFFFFF))
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, Color(0xFFEBEBEB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SimCard, contentDescription = null, tint = Color(0xFF171717), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "SIM ${config.simSlot}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF171717)
                    )
                }

                val carrierLabel = when {
                    !hasPhonePermission -> "Cần quyền đọc SIM"
                    config.subscriptionId == -1 -> "Chưa phát hiện SIM"
                    config.carrierName.isBlank() -> "Chưa xác định"
                    else -> config.carrierName
                }
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color(0xFFEBEBEB))
                ) {
                    Text(
                        text = carrierLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF171717)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val safeLimit = config.dailyLimit.coerceAtLeast(1)
            val progress = (config.sentCountToday.toFloat() / safeLimit).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hạn mức hôm nay", style = MaterialTheme.typography.labelSmall, color = Color(0xFF888888))
                Text(
                    "${config.sentCountToday} / ${config.dailyLimit} tin",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (progress >= 1f) Color(0xFFEE0000) else Color(0xFF171717),
                trackColor = Color(0xFFEBEBEB)
            )

            Spacer(modifier = Modifier.height(12.dp))
            val defaultCarrierLimit = SimConfig.getDefaultDailyLimit(config.carrierName)
            val carrierHintText = if (config.carrierName.isNotBlank()) {
                "Mặc định ${config.carrierName}: $defaultCarrierLimit tin/ngày"
            } else {
                "Khuyến nghị mặc định: $defaultCarrierLimit tin/ngày"
            }

            Text(
                "ĐIỀU CHỈNH HẠN MỨC",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF888888)
            )
            Spacer(modifier = Modifier.height(4.dp))
            var limitText by remember(config.dailyLimit) { mutableStateOf(config.dailyLimit.toString()) }
            val parsedLimit = limitText.toIntOrNull()
            val isLimitValid = parsedLimit != null && parsedLimit > 0

            OutlinedTextField(
                value = limitText,
                onValueChange = { limitText = it.filter(Char::isDigit) },
                label = { Text("Giới hạn tin nhắn mỗi ngày", fontSize = 12.sp) },
                supportingText = {
                    Text(if (isLimitValid) "Áp dụng cho SIM ${config.simSlot}. $carrierHintText" else "Nhập số lớn hơn 0.")
                },
                isError = !isLimitValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFFFFF),
                    unfocusedContainerColor = Color(0xFFFFFFFF),
                    focusedBorderColor = Color(0xFF171717),
                    unfocusedBorderColor = Color(0xFFEBEBEB)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (config.carrierName.isNotBlank() && parsedLimit != defaultCarrierLimit) {
                    OutlinedButton(
                        onClick = { limitText = defaultCarrierLimit.toString() },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF171717))
                    ) {
                        Text("Đặt theo ${config.carrierName} ($defaultCarrierLimit)", fontSize = 12.sp)
                    }
                }
                Button(
                    onClick = {
                        parsedLimit?.let { limit -> onSaveLimit(limit) }
                    },
                    enabled = isLimitValid && parsedLimit != config.dailyLimit,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF171717),
                        contentColor = Color(0xFFFFFFFF),
                        disabledContainerColor = Color(0xFFEBEBEB),
                        disabledContentColor = Color(0xFF888888)
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.padding(3.dp))
                    Text("Lưu hạn mức", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
