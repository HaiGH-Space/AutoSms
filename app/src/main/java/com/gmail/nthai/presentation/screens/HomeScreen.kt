package com.gmail.nthai.presentation.screens

import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.gmail.nthai.domain.model.SimConfig
import com.gmail.nthai.domain.model.UpdateState
import com.gmail.nthai.presentation.components.DownloadProgressDialog
import com.gmail.nthai.presentation.components.DownloadedInstallDialog
import com.gmail.nthai.presentation.components.LoadingDialog
import com.gmail.nthai.presentation.components.NoUpdateAvailableDialog
import com.gmail.nthai.presentation.components.SelectPhoneColumnDialog
import com.gmail.nthai.presentation.components.UpdateAvailableDialog
import com.gmail.nthai.presentation.components.UpdateErrorDialog
import com.gmail.nthai.presentation.viewmodel.ConfigViewModel
import com.gmail.nthai.presentation.viewmodel.CustomerViewModel
import com.gmail.nthai.presentation.viewmodel.HomeDashboardViewModel
import com.gmail.nthai.presentation.viewmodel.UpdateViewModel

@Composable
fun HomeScreen(
    onSelectSim: (Int) -> Unit,
    configViewModel: ConfigViewModel = hiltViewModel(),
    dashboardViewModel: HomeDashboardViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val updateState by updateViewModel.updateState.collectAsStateWithLifecycle()
    val simConfigs by configViewModel.simConfigs.collectAsStateWithLifecycle()
    val dashboardStats by dashboardViewModel.stats.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val pendingImportState by customerViewModel.pendingImportState.collectAsStateWithLifecycle()
    val isImporting by customerViewModel.isImporting.collectAsStateWithLifecycle()
    val importLoadingMessage by customerViewModel.importLoadingMessage.collectAsStateWithLifecycle()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Toast.makeText(context, "Không thể mở file Excel đã chọn.", Toast.LENGTH_LONG).show()
                } else {
                    customerViewModel.prepareExcelImport(inputStream) { err ->
                        Toast.makeText(context, "Lỗi đọc tiêu đề Excel: ${err.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Khởi tạo file thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
    val sim2Config = simConfigs.find { it.simSlot == 2 }

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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Tổng quan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SimStatusCard(
                    simSlot = 1,
                    config = sim1Config,
                    modifier = Modifier.weight(1f)
                )
                SimStatusCard(
                    simSlot = 2,
                    config = sim2Config,
                    modifier = Modifier.weight(1f)
                )
            }

            ExcelImportCard(
                totalCustomers = dashboardStats.totalCustomers,
                readyCount = dashboardStats.sim1ReadyCount + dashboardStats.sim2ReadyCount,
                onClick = {
                    filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                }
            )

            Text(
                text = "Điều phối theo SIM",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SimDispatchCard(
                    simSlot = 1,
                    config = sim1Config,
                    assignedCount = dashboardStats.sim1AssignedCount,
                    readyCount = dashboardStats.sim1ReadyCount,
                    onClick = { onSelectSim(1) },
                    modifier = Modifier.weight(1f)
                )
                SimDispatchCard(
                    simSlot = 2,
                    config = sim2Config,
                    assignedCount = dashboardStats.sim2AssignedCount,
                    readyCount = dashboardStats.sim2ReadyCount,
                    onClick = { onSelectSim(2) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    pendingImportState?.let { state ->
        SelectPhoneColumnDialog(
            headers = state.headers,
            selectedHeader = state.selectedHeader,
            onSelectHeader = { customerViewModel.updateSelectedImportHeader(it) },
            onConfirm = {
                customerViewModel.confirmImport { result ->
                    result.onSuccess { count ->
                        Toast.makeText(context, "Đã nhập $count khách hàng", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        Toast.makeText(context, "Lỗi đọc file Excel: ${err.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDismiss = {
                customerViewModel.cancelImport()
            }
        )
    }

    if (isImporting) {
        LoadingDialog(message = importLoadingMessage)
    }
}

@Composable
private fun SimStatusCard(
    simSlot: Int,
    config: SimConfig?,
    modifier: Modifier = Modifier
) {
    val detected = config?.subscriptionId != null && config.subscriptionId != -1
    ElevatedCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = if (detected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    imageVector = if (detected) Icons.Default.CheckCircle else Icons.Default.SimCard,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = if (detected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("SIM $simSlot", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = if (config == null) "Đang kiểm tra"
                    else if (detected) config.carrierName.ifBlank { "Đã nhận diện" }
                    else "Chưa phát hiện SIM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ExcelImportCard(
    totalCustomers: Int,
    readyCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.FileUpload,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text("Nhập danh sách Excel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = "$totalCustomers khách hàng trong danh sách · $readyCount sẵn sàng",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text("Chọn tệp Excel")
            }
        }
    }
}

@Composable
private fun SimDispatchCard(
    simSlot: Int,
    config: SimConfig?,
    assignedCount: Int,
    readyCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.SimCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("SIM $simSlot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(
                config?.carrierName?.takeIf { it.isNotBlank() } ?: "Danh sách được gán",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text("$assignedCount đã gán", style = MaterialTheme.typography.bodyMedium)
            Text(
                "$readyCount sẵn sàng",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text("Quản lý")
            }
        }
    }
}
