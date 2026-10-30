package com.gmail.nthai.domain.model

import java.io.File

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UpdateState
    data class NoUpdateAvailable(val currentVersionName: String) : UpdateState
    data class Downloading(
        val progressPercent: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : UpdateState
    data class Downloaded(val apkFile: File, val updateInfo: UpdateInfo) : UpdateState
    data class Error(val message: String) : UpdateState
}
