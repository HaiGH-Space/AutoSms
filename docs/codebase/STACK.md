# Technology Stack

## Core Sections (Required)

### 1) Runtime Summary

| Area | Value | Evidence |
|------|-------|----------|
| Primary language | Kotlin 2.0.21 | `gradle/libs.versions.toml#L6`, `build.gradle.kts#L3` |
| Runtime + version | Android SDK (compileSdk 35, minSdk 26, targetSdk 35), Java 11 (build on JDK 21 in CI) | `app/build.gradle.kts#L10-L19`, `app/build.gradle.kts#L53-L56`, `gradle/libs.versions.toml#L2-L4`, `.github/workflows/ci.yml#L18` |
| Application version | Version Name: 0.2.1, Version Code: 12 (Release Type: alpha) | `app/build.gradle.kts#L16-L17`, `update.json#L2-L4` |
| Package manager | Gradle 8.x / AGP 9.4.1 (via Gradle Wrapper) | `gradle/libs.versions.toml#L5`, `gradle/wrapper/gradle-wrapper.properties` |
| Module/build system | Gradle Kotlin DSL (`build.gradle.kts`) with KSP 2.0.21-1.0.28 | `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml#L7` |

### 2) Production Frameworks and Dependencies

List only high-impact production dependencies (frameworks, data, transport, auth).

| Dependency | Version | Role in system | Evidence |
|------------|---------|----------------|----------|
| Jetpack Compose BOM | 2024.12.01 | Declarative UI framework (Material 3, UI Graphics, Tooling Preview, Extended Icons) | `gradle/libs.versions.toml#L15`, `app/build.gradle.kts#L82-L87` |
| Navigation Compose | 2.8.5 | Single-activity in-app screen navigation routing | `gradle/libs.versions.toml#L18`, `app/build.gradle.kts#L91` |
| Hilt (Dagger) | 2.60.1 | Compile-time dependency injection across application, ViewModels, and services | `gradle/libs.versions.toml#L19`, `app/build.gradle.kts#L95-L99` |
| Room Database | 2.8.5 | Local relational SQLite database management (Entities, DAOs, Migrations, Schema v15) | `gradle/libs.versions.toml#L21`, `app/build.gradle.kts#L101-L103`, `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt#L39` |
| AndroidX WorkManager | 2.10.0 | Deferrable background work scheduling (Daily counter resets via `DailySmsWorker`) | `gradle/libs.versions.toml#L22`, `app/build.gradle.kts#L106` |
| Apache POI (`poi-ooxml`) | 5.2.5 | Spreadsheet reading and generation (`.xlsx`, `.xls` streaming SAX parser and SXSSFWorkbook export) | `gradle/libs.versions.toml#L23`, `app/build.gradle.kts#L109` |
| Kotlinx Coroutines | 1.9.0 | Asynchronous flow execution and background service job management | `gradle/libs.versions.toml#L24`, `app/build.gradle.kts#L112-L113` |
| Android Telephony, Calls & Accessibility | Android Platform SDK 35 | Device SIM slot management, native SMS dispatching, auto call execution, and Zalo UI automation | `app/src/main/AndroidManifest.xml#L5-L23`, `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt`, `app/src/main/java/com/gmail/nthai/service/AutoCallForegroundService.kt`, `app/src/main/java/com/gmail/nthai/service/ZaloAccessibilityService.kt` |

### 3) Development Toolchain

| Tool | Purpose | Evidence |
|------|---------|----------|
| KSP (Kotlin Symbol Processing) | Annotation processing for Hilt and Room | `gradle/libs.versions.toml#L76`, `app/build.gradle.kts#L4` |
| ProGuard / R8 | Code shrinking, resource minification, and reflection preservation | `app/build.gradle.kts#L38-L48`, `app/proguard-rules.pro` |
| Gradle Configuration Cache | Faster task configuration and build optimization | `gradle.properties` |
| JUnit 4 / AndroidX JUnit / Espresso | Unit testing and instrumented Android UI navigation tests | `gradle/libs.versions.toml#L10-L12`, `app/build.gradle.kts#L116-L123` |
| GitHub Actions CI | Automated CI testing and APK assembly on master/main pushes and PRs | `.github/workflows/ci.yml` |

### 4) Key Commands

```bash
# Compile and build debug APK (output: app/build/outputs/apk/debug/app-debug.apk)
./gradlew assembleDebug

# Compile and package release APK with ProGuard/R8 minification
./gradlew assembleRelease

# Run all local JVM unit tests
./gradlew test

# Run unit tests specifically for the app module
./gradlew :app:testDebugUnitTest

# Validate update.json version alignment with build.gradle.kts
./gradlew :app:validateUpdateJson

# Sync update.json with current build.gradle.kts version info
./gradlew :app:syncUpdateJson

# Run instrumented Android UI tests on an emulator or device
./gradlew connectedAndroidTest
```

### 5) Environment and Config

- Config sources: `local.properties` (Android SDK location), `gradle.properties` (JVM memory `-Xmx2048m`, `org.gradle.configuration-cache=true`, `kotlin.code.style=official`), `gradle/libs.versions.toml` (version catalog)
- Required env vars: None required for local building. Android SDK environment variable (`ANDROID_HOME` or `sdk.dir` in `local.properties`) and JDK 11+ (`JAVA_HOME`) are required by Gradle.
- Deployment/runtime constraints: Target Android devices must run Android 8.0 (API 26) or higher (`minSdk = 26`), have active cellular SIM cards for SMS dispatch and voice calling, and require manual user permission grants for runtime permissions (`SEND_SMS`, `CALL_PHONE`, `READ_PHONE_STATE`, `READ_CALL_LOG`, `POST_NOTIFICATIONS`, `BIND_ACCESSIBILITY_SERVICE`).

### 6) Evidence

- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `gradle.properties`
- `update.json`
- `.github/workflows/ci.yml`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt`
- `app/proguard-rules.pro`
