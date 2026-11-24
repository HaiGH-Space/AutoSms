package com.gmail.nthai.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.edit
import com.gmail.nthai.domain.model.ReleaseType
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
    private var hasAutoCheckedInSession = false

    companion object {
        const val KEY_UPDATE_URL = "custom_update_url"
        const val KEY_DISMISSED_VERSION_CODE = "dismissed_version_code"
        const val KEY_INCLUDE_PRERELEASE = "include_prerelease"
        const val DEFAULT_UPDATE_URL =
            "https://raw.githubusercontent.com/HaiGH-Space/AutoSms/master/update.json"
    }

    override fun isIncludePrerelease(): Boolean {
        return prefs.getBoolean(KEY_INCLUDE_PRERELEASE, false)
    }

    override suspend fun setIncludePrerelease(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            prefs.edit { putBoolean(KEY_INCLUDE_PRERELEASE, enabled) }
        }
    }

    override fun getUpdateUrl(): String {
        return prefs.getString(KEY_UPDATE_URL, DEFAULT_UPDATE_URL) ?: DEFAULT_UPDATE_URL
    }

    override suspend fun saveUpdateUrl(url: String) {
        val trimmed = url.trim()
        require(trimmed.startsWith("https://", ignoreCase = true)) {
            "Update URL must use HTTPS"
        }
        withContext(Dispatchers.IO) {
            prefs.edit { putString(KEY_UPDATE_URL, trimmed) }
        }
    }

    override fun getDismissedVersionCode(): Int {
        return prefs.getInt(KEY_DISMISSED_VERSION_CODE, 0)
    }

    override suspend fun setDismissedVersionCode(versionCode: Int) {
        withContext(Dispatchers.IO) {
            prefs.edit { putInt(KEY_DISMISSED_VERSION_CODE, versionCode) }
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

    override suspend fun checkForUpdate(
        isManualCheck: Boolean,
        urlOverride: String?,
        includePrerelease: Boolean
    ): Result<UpdateInfo> {
        return withContext(Dispatchers.IO) {
            if (!isManualCheck && hasAutoCheckedInSession) {
                return@withContext Result.failure(Exception("Already auto-checked in this session"))
            }
            if (!isManualCheck) {
                hasAutoCheckedInSession = true
            }
            try {
                val targetUrl = urlOverride?.ifBlank { null } ?: getUpdateUrl()
                if (!targetUrl.startsWith("https://", ignoreCase = true)) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Update URL must use HTTPS protocol: $targetUrl")
                    )
                }
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

                val info = parseUpdateInfoFromJson(jsonString, includePrerelease)
                Result.success(info)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    internal fun parseUpdateInfoFromJson(jsonString: String, includePrerelease: Boolean): UpdateInfo {
        val root = JSONObject(jsonString)

        val releaseObj = root.optJSONObject("release")
        val prereleaseObj = root.optJSONObject("prerelease")
            ?: root.optJSONObject("preRelease")
            ?: root.optJSONObject("beta")

        if (releaseObj != null || prereleaseObj != null) {
            val releaseInfo = releaseObj?.let { parseSingleUpdateInfo(it, defaultType = ReleaseType.OFFICIAL) }
            val prereleaseInfo = prereleaseObj?.let { parseSingleUpdateInfo(it, defaultType = ReleaseType.BETA) }

            return when {
                includePrerelease -> {
                    // Khi người dùng bật nhận bản thử nghiệm:
                    // Ưu tiên bản có versionCode cao hơn giữa prerelease và release
                    when {
                        releaseInfo != null && prereleaseInfo != null -> {
                            if (prereleaseInfo.versionCode >= releaseInfo.versionCode) {
                                prereleaseInfo
                            } else {
                                releaseInfo
                            }
                        }
                        prereleaseInfo != null -> prereleaseInfo
                        releaseInfo != null -> releaseInfo
                        else -> throw IllegalArgumentException("Cả hai kênh release và prerelease đều rỗng")
                    }
                }
                else -> {
                    // Chỉ nhận kênh release chính thức
                    releaseInfo ?: throw IllegalArgumentException("Kênh release chính thức không có trong cấu hình cập nhật")
                }
            }
        }

        // Tương thích ngược: nếu JSON là dạng phẳng (flat) truyền thống
        val single = parseSingleUpdateInfo(root, defaultType = ReleaseType.OFFICIAL)
        if (!includePrerelease && single.releaseType != ReleaseType.OFFICIAL) {
            // Nếu người dùng không muốn nhận pre-release nhưng file chỉ có bản alpha/beta
            // Để an toàn, trả về single nhưng giữ đúng releaseType (hoặc ném thông báo nếu không phù hợp)
        }
        return single
    }

    private fun parseSingleUpdateInfo(json: JSONObject, defaultType: ReleaseType): UpdateInfo {
        val versionCode = json.optInt("versionCode", 0)
        val versionName = json.optString("versionName", "")
        val downloadUrl = json.optString("downloadUrl", "")
        val releaseNotes = json.optString("releaseNotes", "")
        val forceUpdate = json.optBoolean("forceUpdate", false)
        val releaseTypeStr = json.optString("releaseType", "")
        val releaseType = if (releaseTypeStr.isNotBlank()) {
            ReleaseType.from(releaseTypeStr, versionName, releaseNotes)
        } else {
            ReleaseType.from(null, versionName, releaseNotes).takeIf { it != ReleaseType.OFFICIAL } ?: defaultType
        }

        if (downloadUrl.isBlank()) {
            throw IllegalArgumentException("File JSON thiếu đường dẫn downloadUrl!")
        }

        if (!downloadUrl.startsWith("https://", ignoreCase = true)) {
            throw IllegalArgumentException("Download URL must use HTTPS protocol: $downloadUrl")
        }

        return UpdateInfo(
            versionCode = versionCode,
            versionName = versionName,
            downloadUrl = downloadUrl,
            releaseNotes = releaseNotes,
            forceUpdate = forceUpdate,
            releaseType = releaseType
        )
    }

    override suspend fun downloadApk(
        downloadUrl: String,
        outputFile: File,
        onProgress: (progressPercent: Int, bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                if (!downloadUrl.startsWith("https://", ignoreCase = true)) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Download URL must use HTTPS protocol: $downloadUrl")
                    )
                }

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
            val downloadsDir =
                context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
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
            if (!currentUrl.startsWith("https://", ignoreCase = true)) {
                throw SecurityException("Insecure HTTP redirect rejected: $currentUrl")
            }
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
