package com.gmail.nthai.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmail.nthai.domain.model.UpdateInfo
import java.io.File
import java.util.Locale

@Composable
fun UpdateAvailableDialog(
    updateInfo: UpdateInfo,
    onUpdateConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!updateInfo.forceUpdate) onDismiss()
        },
        icon = {
            Icon(
                Icons.Default.SystemUpdate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                "Có phiên bản mới (v${updateInfo.versionName})",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                if (updateInfo.releaseNotes.isNotBlank()) {
                    Text(
                        "Nội dung thay đổi:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        updateInfo.releaseNotes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        "Đã có bản cập nhật mới cho AutoSms. Vui lòng cập nhật để trải nghiệm tính năng mới nhất.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onUpdateConfirm) {
                Text("Cập nhật ngay")
            }
        },
        dismissButton = {
            if (!updateInfo.forceUpdate) {
                TextButton(onClick = onDismiss) {
                    Text("Để sau")
                }
            }
        }
    )
}

@Composable
fun DownloadProgressDialog(
    progressPercent: Int,
    bytesDownloaded: Long,
    totalBytes: Long,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Không cho đóng khi đang tải */ },
        title = {
            Text("Đang tải bản cập nhật...", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                if (progressPercent >= 0) {
                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val downloadedMb = bytesDownloaded / (1024f * 1024f)
                    val totalMb = totalBytes / (1024f * 1024f)
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%.1f MB / %.1f MB (%d%%)",
                            downloadedMb,
                            totalMb,
                            progressPercent
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    val downloadedMb = bytesDownloaded / (1024f * 1024f)
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "Đã tải: %.1f MB",
                            downloadedMb
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onCancel) {
                Text("Hủy tải")
            }
        }
    )
}

@Composable
fun DownloadedInstallDialog(
    apkFile: File,
    updateInfo: UpdateInfo,
    onInstall: (File) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text("Tải hoàn tất!", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "Phiên bản ${updateInfo.versionName} đã sẵn sàng cài đặt. Nhấp \"Cài đặt ngay\" để tiến hành.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = { onInstall(apkFile) }) {
                Text("Cài đặt ngay")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun NoUpdateAvailableDialog(
    currentVersionName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text("Đã là phiên bản mới nhất", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "Bạn đang sử dụng phiên bản v$currentVersionName - phiên bản mới nhất của ứng dụng.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun UpdateErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Lỗi kiểm tra/cập nhật", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(errorMessage, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}
