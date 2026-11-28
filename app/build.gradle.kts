plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.gmail.nthai"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.gmail.nthai"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        val rawVersionName = (project.findProperty("VERSION_NAME") as String?) ?: "0.2.1-alpha"
        fun calculateVersionCode(name: String): Int {
            val semverRegex = Regex("""^v?(\d+)\.(\d+)\.(\d+)(?:-(alpha|beta)(?:\.(\d+))?)?""")
            val match = semverRegex.find(name) ?: return 12
            val major = match.groupValues[1].toInt()
            val minor = match.groupValues[2].toInt()
            val patch = match.groupValues[3].toInt()
            val channel = match.groupValues[4]
            val buildNum = match.groupValues[5].toIntOrNull() ?: 1

            val channelOffset = when (channel.lowercase()) {
                "alpha" -> (1 + (buildNum - 1)).coerceIn(1, 19)
                "beta" -> (20 + (buildNum - 1)).coerceIn(20, 89)
                else -> 90
            }
            return major * 10000 + minor * 1000 + patch * 100 + channelOffset
        }

        val calculatedVersionCode = (project.findProperty("VERSION_CODE") as String?)?.toIntOrNull()
            ?: calculateVersionCode(rawVersionName)

        versionCode = calculatedVersionCode
        versionName = rawVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val storeFileProp = project.findProperty("RELEASE_STORE_FILE") as String? ?: System.getenv("RELEASE_STORE_FILE")
            val storePasswordProp = project.findProperty("RELEASE_STORE_PASSWORD") as String? ?: System.getenv("RELEASE_STORE_PASSWORD")
            val keyAliasProp = project.findProperty("RELEASE_KEY_ALIAS") as String? ?: System.getenv("RELEASE_KEY_ALIAS")
            val keyPasswordProp = project.findProperty("RELEASE_KEY_PASSWORD") as String? ?: System.getenv("RELEASE_KEY_PASSWORD")

            if (storeFileProp != null && file(storeFileProp).exists()) {
                storeFile = file(storeFileProp)
                storePassword = storePasswordProp
                keyAlias = keyAliasProp
                keyPassword = keyPasswordProp
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")?.takeIf { it.storeFile?.exists() == true }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Apache POI
    implementation(libs.poi.ooxml)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("org.json:json:20240303")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.register("validateUpdateJson") {
    group = "verification"
    description = "Validates that update.json is valid and contains required update configurations"
    val updateJsonFile = layout.projectDirectory.file("../update.json").asFile
    doLast {
        if (!updateJsonFile.exists()) {
            throw GradleException("update.json does not exist at root directory!")
        }
        val jsonText = updateJsonFile.readText()
        if (!jsonText.contains(""""downloadUrl":""")) {
            throw GradleException("update.json missing downloadUrl!")
        }
        logger.lifecycle("✅ update.json is present and valid!")
    }
}

tasks.register("syncUpdateJson") {
    group = "publishing"
    description = "Syncs versionCode and versionName in update.json from project properties or build.gradle.kts"
    val updateJsonFile = layout.projectDirectory.file("../update.json").asFile
    val targetVersionName = (project.findProperty("VERSION_NAME") as String?) ?: android.defaultConfig.versionName.orEmpty()
    val targetVersionCode = (project.findProperty("VERSION_CODE") as String?)?.toIntOrNull() ?: (android.defaultConfig.versionCode ?: 0)
    val isPrerelease = targetVersionName.contains("-alpha", ignoreCase = true) || targetVersionName.contains("-beta", ignoreCase = true)
    val targetReleaseNotes = (project.findProperty("RELEASE_NOTES") as String?)
    doLast {
        if (!updateJsonFile.exists()) {
            throw GradleException("update.json does not exist at root directory!")
        }
        val slurper = groovy.json.JsonSlurper()
        @Suppress("UNCHECKED_CAST")
        val jsonMap = (slurper.parse(updateJsonFile, "UTF-8") as? MutableMap<String, Any?>) ?: mutableMapOf()

        val channelKey = if (isPrerelease) "prerelease" else "release"
        @Suppress("UNCHECKED_CAST")
        val channelObj = (jsonMap[channelKey] as? MutableMap<String, Any?>) ?: mutableMapOf()

        channelObj["versionCode"] = targetVersionCode
        channelObj["versionName"] = targetVersionName
        channelObj["downloadUrl"] = "https://github.com/HaiGH-Space/AutoSms/releases/download/v$targetVersionName/AutoSms-v$targetVersionName.apk"
        if (!targetReleaseNotes.isNullOrBlank()) {
            channelObj["releaseNotes"] = targetReleaseNotes
        }
        if (isPrerelease) {
            val type = if (targetVersionName.contains("alpha", ignoreCase = true)) "alpha" else "beta"
            channelObj["releaseType"] = type
        } else {
            channelObj["releaseType"] = "official"
        }
        channelObj["forceUpdate"] = channelObj["forceUpdate"] ?: false

        jsonMap[channelKey] = channelObj

        val outputJson = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(jsonMap))
        updateJsonFile.writeText(outputJson + "\n", Charsets.UTF_8)
        logger.lifecycle("✅ Synced update.json channel [$channelKey] with versionName=$targetVersionName, versionCode=$targetVersionCode")

        // Tương thích ngược với các phiên bản app cũ (như v0.2.0) chỉ đọc root JSON:
        // Luôn đồng bộ các trường ở root theo kênh "release" chính thức
        @Suppress("UNCHECKED_CAST")
        val rootReleaseObj = jsonMap["release"] as? Map<String, Any?>
        if (rootReleaseObj != null) {
            rootReleaseObj.forEach { (key, value) ->
                jsonMap[key] = value
            }
            val legacyCompatibleJson = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(jsonMap))
            updateJsonFile.writeText(legacyCompatibleJson + "\n", Charsets.UTF_8)
            logger.lifecycle("✅ Synced root fields with official release for legacy app compatibility")
        }
    }
}