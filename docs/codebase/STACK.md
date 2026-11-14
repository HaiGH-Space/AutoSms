# Technology Stack

## Core Sections (Required)

### 1) Runtime Summary

| Area | Value | Evidence |
|------|-------|----------|
| Primary language | Kotlin 2.0.21 | `gradle/libs.versions.toml#L3`, `app/build.gradle.kts#L3` |
| Runtime + version | Android SDK (compileSdk 35, minSdk 26, targetSdk 35), Java 11 | `app/build.gradle.kts#L10-L19`, `app/build.gradle.kts#L33-L35` |
| Package manager | Gradle 8.x / AGP 9.4.1 (via Gradle Wrapper) | `gradle/libs.versions.toml#L2`, `gradle/wrapper/gradle-wrapper.properties` |
| Module/build system | Gradle Kotlin DSL (`build.gradle.kts`) with KSP 2.0.21-1.0.28 | `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml#L4` |

### 2) Production Frameworks and Dependencies

List only high-impact production dependencies (frameworks, data, transport, auth).

| Dependency | Version | Role in system | Evidence |
|------------|---------|----------------|----------|
| Jetpack Compose BOM | 2024.12.01 | Declarative UI framework (Material 3, UI Graphics, Tooling Preview) | `gradle/libs.versions.toml#L12`, `app/build.gradle.kts#L61-L66` |
| Navigation Compose | 2.8.5 | Single-activity in-app screen navigation routing | `gradle/libs.versions.toml#L15`, `app/build.gradle.kts#L70` |
| Hilt (Dagger) | 2.60.1 | Compile-time dependency injection across application, ViewModels, and services | `gradle/libs.versions.toml#L16`, `app/build.gradle.kts#L74-L77` |
| Room Database | 2.8.5 | Local relational SQLite database management (Entities, DAOs, Migrations) | `gradle/libs.versions.toml#L18`, `app/build.gradle.kts#L80-L82` |
| AndroidX WorkManager | 2.10.0 | Deferrable background work scheduling (Daily counter resets) | `gradle/libs.versions.toml#L19`, `app/build.gradle.kts#L85` |
| Apache POI (`poi-ooxml`) | 5.2.5 | Spreadsheet reading and generation (`.xlsx`, `.xls` parsing and export) | `gradle/libs.versions.toml#L20`, `app/build.gradle.kts#L88` |
| Kotlinx Coroutines | 1.9.0 | Asynchronous flow execution and background service job management | `gradle/libs.versions.toml#L21`, `app/build.gradle.kts#L91-L92` |
| Android Telephony & Accessibility | Android Platform SDK 35 | Device SIM slot management, native SMS dispatching, and Zalo UI automation | `app/src/main/AndroidManifest.xml#L5-L16`, `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt` |

### 3) Development Toolchain

| Tool | Purpose | Evidence |
|------|---------|----------|
| KSP (Kotlin Symbol Processing) | Annotation processing for Hilt and Room | `gradle/libs.versions.toml#L73`, `app/build.gradle.kts#L4` |
| ProGuard / R8 | Code shrinking, resource minification, and reflection preservation | `app/build.gradle.kts#L23-L30`, `app/proguard-rules.pro` |
| Gradle Configuration Cache | Faster task configuration and build optimization | `gradle.properties#L232` |
| JUnit 4 / AndroidX JUnit / Espresso | Unit testing and instrumented Android UI navigation tests | `gradle/libs.versions.toml#L7-L9`, `app/build.gradle.kts#L95-L101` |

### 4) Key Commands

```bash
# Compile and build debug APK (output: app/build/outputs/apk/debug/app-debug.apk)
./gradlew assembleDebug

# Compile and package release APK with ProGuard/R8 minification
./gradlew assembleRelease

# Run all local unit tests (parsers, usecases, models)
./gradlew test

# Run connected Android instrumented tests on an emulator or device
./gradlew connectedAndroidTest
```

### 5) Environment and Config

- Config sources: `local.properties` (Android SDK location), `gradle.properties` (JVM memory `-Xmx2048m`, `org.gradle.configuration-cache=true`), `gradle/libs.versions.toml` (version catalog)
- Required env vars: None required for building. Android SDK environment variable (`ANDROID_HOME` or `sdk.dir` in `local.properties`) is required by Gradle.
- Deployment/runtime constraints: Target Android devices must run Android 8.0 (API 26) or higher (`minSdk = 26`), have active cellular SIM cards for SMS dispatch, and require manual user permission grants for runtime permissions (`SEND_SMS`, `READ_PHONE_STATE`, `POST_NOTIFICATIONS`, `BIND_ACCESSIBILITY_SERVICE`).

### 6) Evidence

- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `gradle.properties`
- `app/src/main/AndroidManifest.xml`
- `app/proguard-rules.pro`

