package com.gmail.nthai.data.repository

import com.gmail.nthai.domain.model.ReleaseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class UpdateJsonParserTest {

    @Test
    fun parseUpdateInfoFromJson_returnsRelease_whenIncludePrereleaseIsFalse() {
        val json = """
            {
              "release": {
                "versionCode": 10,
                "versionName": "1.0.0",
                "downloadUrl": "https://example.com/release.apk",
                "releaseNotes": "Stable note"
              },
              "prerelease": {
                "versionCode": 11,
                "versionName": "1.1.0-beta",
                "downloadUrl": "https://example.com/beta.apk",
                "releaseNotes": "Beta note"
              }
            }
        """.trimIndent()

        val result = UpdateJsonParser.parseUpdateInfoFromJson(json, includePrerelease = false)
        assertEquals(10, result.versionCode)
        assertEquals("1.0.0", result.versionName)
        assertEquals(ReleaseType.OFFICIAL, result.releaseType)
    }

    @Test
    fun parseUpdateInfoFromJson_returnsPrerelease_whenIncludePrereleaseIsTrueAndHigher() {
        val json = """
            {
              "release": {
                "versionCode": 10,
                "versionName": "1.0.0",
                "downloadUrl": "https://example.com/release.apk",
                "releaseNotes": "Stable note"
              },
              "prerelease": {
                "versionCode": 11,
                "versionName": "1.1.0-beta",
                "downloadUrl": "https://example.com/beta.apk",
                "releaseNotes": "Beta note"
              }
            }
        """.trimIndent()

        val result = UpdateJsonParser.parseUpdateInfoFromJson(json, includePrerelease = true)
        assertEquals(11, result.versionCode)
        assertEquals("1.1.0-beta", result.versionName)
        assertEquals(ReleaseType.BETA, result.releaseType)
    }

    @Test
    fun parseUpdateInfoFromJson_returnsRelease_whenIncludePrereleaseIsTrueButReleaseIsHigher() {
        val json = """
            {
              "release": {
                "versionCode": 15,
                "versionName": "1.5.0",
                "downloadUrl": "https://example.com/release.apk"
              },
              "prerelease": {
                "versionCode": 12,
                "versionName": "1.2.0-beta",
                "downloadUrl": "https://example.com/beta.apk"
              }
            }
        """.trimIndent()

        val result = UpdateJsonParser.parseUpdateInfoFromJson(json, includePrerelease = true)
        assertEquals(15, result.versionCode)
        assertEquals("1.5.0", result.versionName)
    }

    @Test
    fun parseUpdateInfoFromJson_supportsLegacyFlatFormat() {
        val json = """
            {
              "versionCode": 12,
              "versionName": "0.2.1",
              "releaseType": "alpha",
              "downloadUrl": "https://example.com/legacy.apk",
              "releaseNotes": "Legacy notes"
            }
        """.trimIndent()

        val result = UpdateJsonParser.parseUpdateInfoFromJson(json, includePrerelease = false)
        assertEquals(12, result.versionCode)
        assertEquals("0.2.1", result.versionName)
        assertEquals(ReleaseType.ALPHA, result.releaseType)
    }

    @Test
    fun parseUpdateInfoFromJson_throwsWhenReleaseMissingAndNotIncludePrerelease() {
        val json = """
            {
              "prerelease": {
                "versionCode": 12,
                "versionName": "1.2.0-beta",
                "downloadUrl": "https://example.com/beta.apk"
              }
            }
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            UpdateJsonParser.parseUpdateInfoFromJson(json, includePrerelease = false)
        }
    }
}
