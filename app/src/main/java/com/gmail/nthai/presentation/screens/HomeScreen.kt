package com.gmail.nthai.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.domain.model.UpdateState
import com.gmail.nthai.presentation.components.DownloadProgressDialog
import com.gmail.nthai.presentation.components.DownloadedInstallDialog
import com.gmail.nthai.presentation.components.NoUpdateAvailableDialog
import com.gmail.nthai.presentation.components.UpdateAvailableDialog
import com.gmail.nthai.presentation.components.UpdateErrorDialog
import com.gmail.nthai.presentation.viewmodel.UpdateViewModel

@Composable
fun HomeScreen(
    onImportExcel: () -> Unit,
    onSelectSim: (Int) -> Unit,
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val updateState by updateViewModel.updateState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        updateViewModel.checkForUpdate(isManualCheck = false)
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
                title = "Danh sách gửi SIM 1",
                subtitle = "Chọn khách hàng và số điện thoại dành riêng cho SIM 1",
                onClick = { onSelectSim(1) },
                icon = { Icon(Icons.Default.SimCard, contentDescription = null) }
            )
            HomeActionCard(
                title = "Danh sách gửi SIM 2",
                subtitle = "Chọn khách hàng và số điện thoại dành riêng cho SIM 2",
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
    icon: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon()
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text("Mở")
            }
        }
    }
}
