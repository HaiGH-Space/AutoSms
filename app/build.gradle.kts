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
        versionCode = 10
        versionName = "0.1.9"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
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
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.register("validateUpdateJson") {
    group = "verification"
    description = "Validates that update.json matches app build version info"
    val updateJsonFile = layout.projectDirectory.file("../update.json").asFile
    val appVersionName = android.defaultConfig.versionName.orEmpty()
    val appVersionCode = android.defaultConfig.versionCode ?: 0
    doLast {
        if (!updateJsonFile.exists()) {
            throw GradleException("update.json does not exist at root directory!")
        }
        val jsonText = updateJsonFile.readText()

        if (!jsonText.contains(""""versionCode": $appVersionCode""")) {
            throw GradleException("update.json versionCode does not match build.gradle.kts ($appVersionCode)")
        }
        if (!jsonText.contains(""""versionName": "$appVersionName"""")) {
            throw GradleException("update.json versionName does not match build.gradle.kts ($appVersionName)")
        }
        logger.lifecycle("✅ update.json version ($appVersionName / $appVersionCode) matches build.gradle.kts!")
    }
}

tasks.register("syncUpdateJson") {
    group = "publishing"
    description = "Syncs versionCode and versionName in update.json from build.gradle.kts"
    val updateJsonFile = layout.projectDirectory.file("../update.json").asFile
    val appVersionName = android.defaultConfig.versionName.orEmpty()
    val appVersionCode = android.defaultConfig.versionCode ?: 0
    doLast {
        if (!updateJsonFile.exists()) {
            throw GradleException("update.json does not exist at root directory!")
        }
        var content = updateJsonFile.readText()
        content = content.replace(Regex(""""versionCode":\s*\d+"""), """"versionCode": $appVersionCode""")
        content = content.replace(Regex(""""versionName":\s*"[^"]+""""), """"versionName": "$appVersionName"""")
        content = content.replace(
            Regex(""""downloadUrl":\s*"https://github.com/HaiGH-Space/AutoSms/releases/download/[^"]+/app-release.apk""""),
            """"downloadUrl": "https://github.com/HaiGH-Space/AutoSms/releases/download/$appVersionName/app-release.apk""""
        )
        updateJsonFile.writeText(content)
        logger.lifecycle("✅ Synced update.json with versionName=$appVersionName, versionCode=$appVersionCode")
    }
}