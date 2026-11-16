package com.gmail.nthai.domain.model

import java.util.Locale

enum class ReleaseType {
    ALPHA,
    BETA,
    OFFICIAL;

    val label: String
        get() = when (this) {
            ALPHA -> "Bản thử nghiệm Alpha"
            BETA -> "Bản thử nghiệm Beta"
            OFFICIAL -> "Bản chính thức"
        }

    companion object {
        fun from(typeString: String?, versionName: String, releaseNotes: String): ReleaseType {
            if (!typeString.isNullOrBlank()) {
                return when (typeString.lowercase(Locale.ROOT)) {
                    "alpha" -> ALPHA
                    "beta" -> BETA
                    "official", "release", "stable" -> OFFICIAL
                    else -> OFFICIAL
                }
            }
            val combined = "$versionName $releaseNotes".lowercase(Locale.ROOT)
            return when {
                combined.contains("alpha") -> ALPHA
                combined.contains("beta") -> BETA
                else -> OFFICIAL
            }
        }
    }
}

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String = "",
    val forceUpdate: Boolean = false,
    val releaseType: ReleaseType = ReleaseType.OFFICIAL,
)
