# External Integrations

## Core Sections (Required)

### 1) Integration Inventory

| System | Type (API/DB/Queue/etc) | Purpose | Auth model | Criticality | Evidence |
|--------|---------------------------|---------|------------|-------------|----------|
| Android Telephony (`SmsManager`) | Android OS Telephony API | Direct cellular SMS dispatching and multipart SMS segmenting | Android runtime permission (`SEND_SMS`, `READ_PHONE_STATE`) | High | `app/src/main/AndroidManifest.xml#L9`, `app/src/main/java/com/gmail/nthai/data/sms/SmsSender.kt` |
| Android Accessibility Service | Android OS System Accessibility Service | UI automation for opening conversations, setting message text, and clicking send in Zalo | Android system permission (`BIND_ACCESSIBILITY_SERVICE`) | Medium | `app/src/main/AndroidManifest.xml#L55-L65`, `app/src/main/java/com/gmail/nthai/service/ZaloAccessibilityService.kt` |
| Zalo Android App (`com.zing.zalo`) | Third-party Android Application | External messaging target opened via deep link (`https://zalo.me/{phone}`) | Local user login session in native Zalo app | Medium | `app/src/main/java/com/gmail/nthai/service/ZaloSendingEngine.kt#L112-L118` |
| GitHub Releases API | Remote HTTP endpoint | Application self-update check and APK download | Public HTTP GET request to `raw.githubusercontent.com` | Low | `app/src/main/java/com/gmail/nthai/data/repository/UpdateRepositoryImpl.kt#L29`, `update.json` |
| Android WorkManager | Background Scheduler API | Periodic execution of daily SIM limit counter resets (`DailySmsWorker`) | Android OS system service | Medium | `app/src/main/java/com/gmail/nthai/service/DailySmsWorker.kt` |
| Android FileProvider | ContentProvider API | Securely sharing exported Excel files and local APK installers with external apps | URI permission grants (`FLAG_GRANT_READ_URI_PERMISSION`) | Medium | `app/src/main/AndroidManifest.xml#L76-L84`, `app/src/main/res/xml/file_paths.xml` |

### 2) Data Stores

| Store | Role | Access layer | Key risk | Evidence |
|-------|------|--------------|----------|----------|
| Room SQLite DB (`auto_sms_database`) | Local persistence for customers, templates, SIM configs, sending rules, and audit logs | Room DAOs (`CustomerDao`, `SendingLogDao`, etc.) injected via Hilt | Schema migration failure can trigger destructive wipe (`fallbackToDestructiveMigration`) | `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt#L77-L86`, `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt` |
| Android `SharedPreferences` (`app_update_prefs`) | Key-value store for custom in-app update URLs | `UpdateRepositoryImpl` | Reset if application data is cleared | `app/src/main/java/com/gmail/nthai/data/repository/UpdateRepositoryImpl.kt#L25` |
| Local File Storage / Cache | Temporary staging for Excel reports and downloaded APK updates | `ExcelExporter.kt`, `ApkInstaller.kt` via `FileProvider` | Device storage exhaustion on large batch operations | `app/src/main/java/com/gmail/nthai/data/excel/ExcelExporter.kt` |

### 3) Secrets and Credentials Handling

- Credential sources: No third-party API keys, OAuth tokens, or client secrets are required. Cellular transmission relies on physical device SIM cards.
- Hardcoding checks: No secrets or credentials are hardcoded. In-app update URL defaults to public repository endpoint: `https://raw.githubusercontent.com/HaiGH-Space/AutoSms/master/update.json`.
- Rotation or lifecycle notes: Not applicable; no private tokens exist in the codebase.

### 4) Reliability and Failure Behavior

- Retry/backoff behavior:
  - SMS: Managed by `ProcessSimSendingUseCase` and `SimSlotSendingController`. Failed sends capture carrier failure error codes via `SmsStatusReceiver`. Retries must respect randomized delay and quota boundaries.
  - Zalo: `ZaloSendingEngine` implements a 15-second timeout waiting for the Accessibility event confirmation (`withTimeoutOrNull(15000L)`). If the timeout lapses, the recipient is flagged as `FAILED`.
- Timeout policy: Configured in `ZaloSendingEngine.kt` (15,000 ms) and standard Android `HttpURLConnection` connect/read timeouts for APK updates.
- Circuit-breaker or fallback behavior: When a SIM slot reaches its configured daily quota or operates outside the allowed hour/minute window, `ValidateSendingConstraintUseCase` pauses or halts sending for that slot.

### 5) Observability for Integrations

- Logging around external calls: Native Logcat output (`android.util.Log.d`, `Log.e`) logs dispatch steps, intent receiver events, and accessibility node interactions.
- Metrics/tracing coverage: High-level metric counters recorded locally in `sending_logs` table (`SENT`, `DELIVERED`, `FAILED`, `CANCELLED`).
- Missing visibility gaps: No remote crash reporting (e.g. Firebase Crashlytics) or remote analytics are configured; failures are diagnosable only on physical devices via Logcat or exported Excel reports.

### 6) Evidence

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/gmail/nthai/data/sms/SmsSender.kt`
- `app/src/main/java/com/gmail/nthai/service/ZaloAccessibilityService.kt`
- `app/src/main/java/com/gmail/nthai/service/ZaloSendingEngine.kt`
- `app/src/main/java/com/gmail/nthai/data/repository/UpdateRepositoryImpl.kt`
- `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt`

