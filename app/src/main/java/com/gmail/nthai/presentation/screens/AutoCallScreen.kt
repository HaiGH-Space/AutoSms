package com.gmail.nthai.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmail.nthai.domain.model.AutoCallStatus
import com.gmail.nthai.presentation.components.autocall.AutoCallConfigCard
import com.gmail.nthai.presentation.components.autocall.AutoCallLogSection
import com.gmail.nthai.presentation.components.autocall.AutoCallProgressCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCallScreen(
    viewModel: AutoCallViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val progressState by viewModel.progressState.collectAsState()
    val callLogs by viewModel.callLogs.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val delaySeconds by viewModel.delaySeconds.collectAsState()
    val simConfigs by viewModel.simConfigs.collectAsState()
    val selectedSimSlot by viewModel.selectedSimSlot.collectAsState()

    var hasCallPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        )
    }

    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        hasCallPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        onPauseOrDispose {}
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val callGranted = permissions[Manifest.permission.CALL_PHONE] ?: false
        val stateGranted = permissions[Manifest.permission.READ_PHONE_STATE] ?: false
        hasCallPermission = callGranted && stateGranted
        if (hasCallPermission) {
            viewModel.startCampaign()
        } else {
            android.widget.Toast.makeText(context, "Vui lòng cấp quyền gọi điện trong Cài đặt ứng dụng", android.widget.Toast.LENGTH_LONG).show()
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tự động gọi điện",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (!hasCallPermission) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3E0),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE65100)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Chưa cấp quyền gọi điện",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Text(
                                    text = "Cần quyền CALL_PHONE và READ_PHONE_STATE để tự động gọi.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFBF360C)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val callGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                                val stateGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                                if (callGranted && stateGranted) {
                                    hasCallPermission = true
                                } else {
                                    try {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.CALL_PHONE,
                                                Manifest.permission.READ_PHONE_STATE
                                            )
                                        )
                                    } catch (_: Exception) {
                                        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = android.net.Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                        ) {
                            Text("Cấp quyền")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            val selectedCount = customers.count { it.isSelected }

            AutoCallConfigCard(
                delaySeconds = delaySeconds,
                onDelayChange = viewModel::updateDelay,
                selectedSimSlot = selectedSimSlot,
                onSimSlotChange = viewModel::updateSimSlot,
                simConfigs = simConfigs,
                totalCustomersCount = customers.size,
                selectedCustomersCount = selectedCount,
                isRunning = progressState.status == AutoCallStatus.RUNNING || progressState.status == AutoCallStatus.PAUSED,
                onStartClick = {
                    if (customers.isEmpty()) {
                        android.widget.Toast.makeText(context, "Vui lòng thêm danh bạ khách hàng trước khi gọi tự động", android.widget.Toast.LENGTH_SHORT).show()
                    } else if (hasCallPermission) {
                        viewModel.startCampaign()
                    } else {
                        try {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.CALL_PHONE,
                                    Manifest.permission.READ_PHONE_STATE
                                )
                            )
                        } catch (_: Exception) {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            )

            if (progressState.status != AutoCallStatus.IDLE) {
                Spacer(modifier = Modifier.height(16.dp))
                AutoCallProgressCard(
                    progress = progressState,
                    onPause = viewModel::pauseCampaign,
                    onResume = viewModel::resumeCampaign,
                    onStop = viewModel::stopCampaign
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AutoCallLogSection(
                logs = callLogs,
                onClearLogs = viewModel::clearCallLogs
            )
        }
    }
}
