package com.gmail.nthai.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.edit
import com.gmail.nthai.domain.model.UpdateInfo
import com.gmail.nthai.domain.repository.UpdateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : UpdateRepository {

    private val prefs = context.getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_UPDATE_URL = "custom_update_url"
        const val DEFAULT_UPDATE_URL = "https://raw.githubusercontent.com/HaiGH-Space/AutoSms/master/update.json"
    }

    override fun getUpdateUrl(): String {
        return prefs.getString(KEY_UPDATE_URL, DEFAULT_UPDATE_URL) ?: DEFAULT_UPDATE_URL
    }

    override suspend fun saveUpdateUrl(url: String) {
        withContext(Dispatchers.IO) {
            prefs.edit { putString(KEY_UPDATE_URL, url.trim()) }
        }
    }

    override fun getCurrentVersionCode(): Int {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (_: Exception) {
            1
        }
    }

    override fun getCurrentVersionName(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }

    override suspend fun checkForUpdate(urlOverride: String?): Result<UpdateInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val targetUrl = urlOverride?.ifBlank { null } ?: getUpdateUrl()
                val connection = openConnectionWithRedirects(targetUrl)
                connection.connectTimeout = 10000
                connection.readTimeout = 15000
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext Result.failure(
                        Exception("HTTP error code: $responseCode")
                    )
                }

                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()

                val json = JSONObject(jsonString)
                val versionCode = json.optInt("versionCode", 0)
                val versionName = json.optString("versionName", "")
                val downloadUrl = json.optString("downloadUrl", "")
                val releaseNotes = json.optString("releaseNotes", "")
                val forceUpdate = json.optBoolean("forceUpdate", false)

                if (downloadUrl.isBlank()) {
                    return@withContext Result.failure(Exception("File JSON thiếu đường dẫn downloadUrl!"))
                }

                val info = UpdateInfo(
                    versionCode = versionCode,
                    versionName = versionName,
                    downloadUrl = downloadUrl,
                    releaseNotes = releaseNotes,
                    forceUpdate = forceUpdate
                )
                Result.success(info)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun downloadApk(
        downloadUrl: String,
        outputFile: File,
        onProgress: (progressPercent: Int, bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                clearOldApks()

                outputFile.parentFile?.mkdirs()
                if (outputFile.exists()) {
                    outputFile.delete()
                }

                val connection = openConnectionWithRedirects(downloadUrl)
                connection.connectTimeout = 15000
                connection.readTimeout = 30000

                val totalBytes = connection.contentLengthLong
                var bytesDownloaded = 0L

                connection.inputStream.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            bytesDownloaded += bytesRead
                            val percent = if (totalBytes > 0) {
                                ((bytesDownloaded * 100) / totalBytes).toInt()
                            } else {
                                -1
                            }
                            onProgress(percent, bytesDownloaded, totalBytes)
                        }
                        output.flush()
                    }
                }
                connection.disconnect()

                Result.success(outputFile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun clearOldApks() {
        try {
            val downloadsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            downloadsDir?.listFiles()?.forEach { file ->
                if (file.name.endsWith(".apk", ignoreCase = true)) {
                    file.delete()
                }
            }

            context.cacheDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".apk", ignoreCase = true)) {
                    file.delete()
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun openConnectionWithRedirects(urlString: String): HttpURLConnection {
        var currentUrl = urlString
        var redirects = 0
        while (redirects < 5) {
            val url = URL(currentUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Android; AutoSms App Update)"
            )

            val code = connection.responseCode
            val isRedirect = when (code) {
                HttpURLConnection.HTTP_MOVED_PERM,
                HttpURLConnection.HTTP_MOVED_TEMP,
                HttpURLConnection.HTTP_SEE_OTHER,
                307,
                308 -> true
                else -> false
            }
            if (isRedirect) {
                val loc = connection.getHeaderField("Location")
                if (!loc.isNullOrBlank()) {
                    currentUrl = loc
                    redirects++
                    connection.disconnect()
                    continue
                }
            }
            return connection
        }
        val finalUrl = URL(currentUrl)
        return finalUrl.openConnection() as HttpURLConnection
    }
}
