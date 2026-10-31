package com.gmail.nthai.domain.model

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String = "",
    val forceUpdate: Boolean = false
)
