package com.gmail.nthai.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun HomeScreen(
    onImportExcel: () -> Unit,
    onSelectSim: (Int) -> Unit,
    configViewModel: ConfigViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val updateState by updateViewModel.updateState.collectAsStateWithLifecycle()
    val simConfigs by configViewModel.simConfigs.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        configViewModel.loadSims()
        updateViewModel.checkForUpdate(isManualCheck = false)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                configViewModel.loadSims()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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

    val sim1Config = simConfigs.find { it.simSlot == 1 }
    val sim1Carrier = sim1Config?.carrierName?.trim()?.takeIf { it.isNotEmpty() }
    val sim1Title = if (sim1Carrier != null) "Danh sách gửi SIM 1 - $sim1Carrier" else "Danh sách gửi SIM 1"
    val sim1Subtitle = if (sim1Carrier != null) {
        "Chọn khách hàng và số điện thoại dành riêng cho SIM 1 ($sim1Carrier)"
    } else {
        "Chọn khách hàng và số điện thoại dành riêng cho SIM 1"
    }
    val sim1Badge = sim1Carrier ?: if (sim1Config?.subscriptionId == -1) "Chưa phát hiện SIM" else null

    val sim2Config = simConfigs.find { it.simSlot == 2 }
    val sim2Carrier = sim2Config?.carrierName?.trim()?.takeIf { it.isNotEmpty() }
    val sim2Title = if (sim2Carrier != null) "Danh sách gửi SIM 2 - $sim2Carrier" else "Danh sách gửi SIM 2"
    val sim2Subtitle = if (sim2Carrier != null) {
        "Chọn khách hàng và số điện thoại dành riêng cho SIM 2 ($sim2Carrier)"
    } else {
        "Chọn khách hàng và số điện thoại dành riêng cho SIM 2"
    }
    val sim2Badge = sim2Carrier ?: if (sim2Config?.subscriptionId == -1) "Chưa phát hiện SIM" else null

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "AutoSms",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HomeActionCard(
                title = "Nhập danh sách Excel",
                subtitle = "Thêm khách hàng và số điện thoại từ file Excel",
                onClick = onImportExcel,
                icon = { Icon(Icons.Default.FileUpload, contentDescription = null) }
            )
            HomeActionCard(
                title = sim1Title,
                subtitle = sim1Subtitle,
                badgeText = sim1Badge,
                onClick = { onSelectSim(1) },
                icon = { Icon(Icons.Default.SimCard, contentDescription = null) }
            )
            HomeActionCard(
                title = sim2Title,
                subtitle = sim2Subtitle,
                badgeText = sim2Badge,
                onClick = { onSelectSim(2) },
                icon = { Icon(Icons.Default.SimCard, contentDescription = null) }
            )
        }
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    badgeText: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                if (!badgeText.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text("Mở")
            }
        }
    }
}
