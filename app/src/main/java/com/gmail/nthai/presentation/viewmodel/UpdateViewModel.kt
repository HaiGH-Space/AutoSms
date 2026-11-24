package com.gmail.nthai.presentation.viewmodel

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.model.UpdateInfo
import com.gmail.nthai.domain.model.UpdateState
import com.gmail.nthai.domain.repository.UpdateRepository
import com.gmail.nthai.util.ApkInstaller
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: UpdateRepository
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _updateUrl = MutableStateFlow(updateRepository.getUpdateUrl())
    val updateUrl: StateFlow<String> = _updateUrl.asStateFlow()

    private val _includePrerelease = MutableStateFlow(updateRepository.isIncludePrerelease())
    val includePrerelease: StateFlow<Boolean> = _includePrerelease.asStateFlow()

    private var downloadJob: Job? = null

    init {
        // Dọn dẹp các file APK cũ rác nếu có khi khởi tạo ViewModel
        viewModelScope.launch(Dispatchers.IO) {
            updateRepository.clearOldApks()
        }
    }

    fun getCurrentVersionName(): String = updateRepository.getCurrentVersionName()

    fun getCurrentVersionCode(): Int = updateRepository.getCurrentVersionCode()

    fun setIncludePrerelease(enabled: Boolean) {
        viewModelScope.launch {
            updateRepository.setIncludePrerelease(enabled)
            _includePrerelease.value = enabled
        }
    }

    fun saveUpdateUrl(url: String) {
        viewModelScope.launch {
            updateRepository.saveUpdateUrl(url)
            _updateUrl.value = updateRepository.getUpdateUrl()
        }
    }

    fun checkForUpdate(isManualCheck: Boolean = false, urlOverride: String? = null) {
        viewModelScope.launch {
            if (isManualCheck) {
                _updateState.value = UpdateState.Checking
            }

            val result = updateRepository.checkForUpdate(
                isManualCheck = isManualCheck,
                urlOverride = urlOverride,
                includePrerelease = _includePrerelease.value
            )
            result.onSuccess { info ->
                val currentCode = updateRepository.getCurrentVersionCode()
                if (info.versionCode > currentCode) {
                    val dismissedCode = updateRepository.getDismissedVersionCode()
                    if (!isManualCheck && info.versionCode <= dismissedCode) {
                        _updateState.value = UpdateState.Idle
                    } else {
                        _updateState.value = UpdateState.UpdateAvailable(info)
                    }
                } else {
                    if (isManualCheck) {
                        _updateState.value =
                            UpdateState.NoUpdateAvailable(updateRepository.getCurrentVersionName())
                    } else {
                        _updateState.value = UpdateState.Idle
                    }
                }
            }.onFailure { e ->
                if (isManualCheck) {
                    _updateState.value = UpdateState.Error(
                        e.localizedMessage ?: "Không thể kết nối tới máy chủ kiểm tra cập nhật!"
                    )
                } else {
                    _updateState.value = UpdateState.Idle
                }
            }
        }
    }

    fun dismissUpdate(versionCode: Int) {
        viewModelScope.launch {
            updateRepository.setDismissedVersionCode(versionCode)
            _updateState.value = UpdateState.Idle
        }
    }

    fun startDownload(context: Context, updateInfo: UpdateInfo) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            _updateState.value = UpdateState.Downloading(0, 0, 0)

            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.cacheDir
            val fileName = "AutoSms-v${updateInfo.versionName}.apk"
            val targetFile = File(downloadsDir, fileName)

            val result = updateRepository.downloadApk(
                downloadUrl = updateInfo.downloadUrl,
                outputFile = targetFile,
                onProgress = { percent, downloaded, total ->
                    _updateState.value = UpdateState.Downloading(percent, downloaded, total)
                }
            )

            result.onSuccess { file ->
                _updateState.value = UpdateState.Downloaded(file, updateInfo)
            }.onFailure { e ->
                _updateState.value = UpdateState.Error(
                    "Tải file APK thất bại: ${e.localizedMessage ?: "Lỗi không xác định"}"
                )
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        viewModelScope.launch(Dispatchers.IO) {
            updateRepository.clearOldApks()
        }
        _updateState.value = UpdateState.Idle
    }

    fun installApk(context: Context, apkFile: File): Boolean {
        if (!ApkInstaller.canInstallPackages(context)) {
            ApkInstaller.openInstallPermissionSettings(context)
            return false
        }
        return ApkInstaller.installApk(context, apkFile)
    }

    fun dismissState() {
        _updateState.value = UpdateState.Idle
    }
}
