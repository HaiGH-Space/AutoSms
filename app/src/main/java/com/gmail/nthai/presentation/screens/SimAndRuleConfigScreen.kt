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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.gmail.nthai.presentation.viewmodel.ConfigViewModel
import com.gmail.nthai.util.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimAndRuleConfigScreen(
    viewModel: ConfigViewModel = hiltViewModel()
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
    val simDetectionMessage by viewModel.simDetectionMessage.collectAsStateWithLifecycle()
    val rule by viewModel.sendingRule.collectAsStateWithLifecycle()
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

    var minDelayText by remember(rule) { mutableStateOf(rule.minDelaySec.toString()) }
    var maxDelayText by remember(rule) { mutableStateOf(rule.maxDelaySec.toString()) }
    var startHourText by remember(rule) { mutableStateOf(rule.startHour.toString()) }
    var endHourText by remember(rule) { mutableStateOf(rule.endHour.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cấu hình SIM & Quy tắc") },
                actions = {
                    Button(
                        onClick = {
                            val min = minDelayText.toIntOrNull() ?: 10
                            val max = maxDelayText.toIntOrNull() ?: 30
                            val start = startHourText.toIntOrNull() ?: 8
                            val end = endHourText.toIntOrNull() ?: 17

                            viewModel.updateRule(min, max, start, end)
                            Toast.makeText(context, "Đã lưu cài đặt!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Lưu quy tắc")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quản lý Hạn mức SIM",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Trạng thái quyền:\n" +
                    "• Quyền Điện thoại (đọc SIM): ${if (hasPhonePermission) "Đã cấp" else "Chưa cấp"}\n" +
                    "• Quyền Gửi SMS: ${if (hasSmsPermission) "Đã cấp" else "Chưa cấp"}"
            )

            if (!hasPhonePermission || !hasSmsPermission) {
                Text(
                    text = "Cần đầy đủ quyền Điện thoại & Gửi SMS để ứng dụng nhận diện 2 SIM và phát tin.",
                    color = MaterialTheme.colorScheme.error
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
                        Text("Mở Cài đặt ứng dụng")
                    }
                }
            }

            simDetectionMessage?.let { message ->
                Text(
                    message,
                    color = if (simConfigs.any { it.subscriptionId != -1 }) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            TextButton(onClick = { viewModel.loadSims() }) {
                Text("Thử nhận diện SIM ngay")
            }

            simConfigs.forEach { config ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SimCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "SIM ${config.simSlot} " + when {
                                    !hasPhonePermission -> "(Chưa cấp quyền Điện thoại)"
                                    config.subscriptionId != -1 -> "(Đã kết nối - ID: ${config.subscriptionId})"
                                    else -> "(Chưa cắm / Chưa nhận dạng)"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Quy tắc khoảng trễ & Khung giờ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(modifier = Modifier.fillMaxWidth()) {
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
                }
            }
        }
    }
}
