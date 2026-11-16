package com.gmail.nthai.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmail.nthai.domain.model.ReleaseType
import com.gmail.nthai.domain.model.UpdateInfo
import java.io.File
import java.util.Locale

private data class BadgeConfig(
    val backgroundColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
    val text: String
)

@Composable
fun ReleaseTypeBadge(
    releaseType: ReleaseType,
    modifier: Modifier = Modifier
) {
    val config = when (releaseType) {
        ReleaseType.ALPHA -> BadgeConfig(
            backgroundColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            icon = Icons.Default.ErrorOutline,
            text = "Bản thử nghiệm Alpha"
        )
        ReleaseType.BETA -> BadgeConfig(
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            icon = Icons.Default.Info,
            text = "Bản thử nghiệm Beta"
        )
        ReleaseType.OFFICIAL -> BadgeConfig(
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = Icons.Default.CheckCircle,
            text = "Bản phát hành chính thức"
        )
    }

    Surface(
        color = config.backgroundColor,
        contentColor = config.contentColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = config.icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = config.text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Có phiên bản mới (v${updateInfo.versionName})",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                ReleaseTypeBadge(releaseType = updateInfo.releaseType)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tải hoàn tất!", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                ReleaseTypeBadge(releaseType = updateInfo.releaseType)
            }
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(errorMessage, style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}
