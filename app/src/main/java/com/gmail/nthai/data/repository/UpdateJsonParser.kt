package com.gmail.nthai.data.repository

import com.gmail.nthai.domain.model.ReleaseType
import com.gmail.nthai.domain.model.UpdateInfo
import org.json.JSONObject

object UpdateJsonParser {

    fun parseUpdateInfoFromJson(jsonString: String, includePrerelease: Boolean): UpdateInfo {
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
        return parseSingleUpdateInfo(root, defaultType = ReleaseType.OFFICIAL)
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
}
