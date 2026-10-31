package com.gmail.nthai.domain.repository

import com.gmail.nthai.domain.model.UpdateInfo
import java.io.File

interface UpdateRepository {
    fun getUpdateUrl(): String
    suspend fun saveUpdateUrl(url: String)
    suspend fun checkForUpdate(urlOverride: String? = null): Result<UpdateInfo>
    suspend fun downloadApk(
        downloadUrl: String,
        outputFile: File,
        onProgress: (progressPercent: Int, bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): Result<File>
    fun clearOldApks()
    fun getCurrentVersionCode(): Int
    fun getCurrentVersionName(): String
}
