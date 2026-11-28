# External Integrations

## Core Sections (Required)

### 1) Integration Inventory

| System | Type (API/DB/Queue/etc) | Purpose | Auth model | Criticality | Evidence |
|--------|---------------------------|---------|------------|-------------|----------|
| Android Telephony (`SmsManager`) | Android OS Telephony API | Direct cellular SMS dispatching and multipart SMS segmenting | Android runtime permission (`SEND_SMS`, `READ_PHONE_STATE`) | High | `app/src/main/AndroidManifest.xml#L11`, `app/src/main/java/com/gmail/nthai/data/sms/SmsSenderEngine.kt` |
| Android Telecom & Call Log | Android OS Telecom / Telephony API | Automated voice call dialing and call duration tracking | Android runtime permission (`CALL_PHONE`, `READ_CALL_LOG`, `READ_PHONE_STATE`) | High | `app/src/main/AndroidManifest.xml#L12-L14`, `app/src/main/java/com/gmail/nthai/domain/telephony/PhoneCallStateObserver.kt`, `app/src/main/java/com/gmail/nthai/service/AutoCallForegroundService.kt` |
| Android Accessibility Service | Android OS System Accessibility Service | UI automation for opening conversations, setting message text, and clicking send in Zalo | Android system permission (`BIND_ACCESSIBILITY_SERVICE`) | Medium | `app/src/main/AndroidManifest.xml#L67-L77`, `app/src/main/java/com/gmail/nthai/service/ZaloAccessibilityService.kt`, `app/src/main/res/xml/zalo_accessibility_config.xml` |
| Zalo Android App (`com.zing.zalo`) | Third-party Android Application | External messaging target opened via deep link (`https://zalo.me/{phone}`) | Local user login session in native Zalo app | Medium | `app/src/main/java/com/gmail/nthai/service/ZaloSendingEngine.kt` |
| GitHub Releases API / Raw Content | Remote HTTP endpoint | Application self-update check and APK download | Public HTTPS GET request to `raw.githubusercontent.com` | Low | `app/src/main/java/com/gmail/nthai/data/repository/UpdateRepositoryImpl.kt#L29`, `update.json` |
| Android WorkManager | Background Scheduler API | Periodic execution of daily SIM limit counter resets (`DailySmsWorker`) | Android OS system service | Medium | `app/src/main/java/com/gmail/nthai/service/DailySmsWorker.kt` |
| Android FileProvider | ContentProvider API | Securely sharing exported Excel files and local APK installers with external apps | URI permission grants (`FLAG_GRANT_READ_URI_PERMISSION`) | Medium | `app/src/main/AndroidManifest.xml#L88-L96`, `app/src/main/res/xml/file_paths.xml` |

### 2) Data Stores

| Store | Role | Access layer | Key risk | Evidence |
|-------|------|--------------|----------|----------|
| Room SQLite DB (`auto_sms_database`, Schema v15) | Local persistence for customers, templates, SIM configs, sending rules, sending logs, Zalo recipients, Zalo logs, Blacklist, Call logs, and Call recipients | Room DAOs (`CustomerDao`, `SendingLogDao`, `ZaloRecipientDao`, `BlacklistDao`, `CallLogDao`, `CallRecipientDao`) injected via Hilt | Schema migration failure triggers destructive wipe (`fallbackToDestructiveMigration`) if older or missing migrations (< v4) are traversed | `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt`, `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt#L39` |
| Android `SharedPreferences` (`app_update_prefs`) | Key-value store for custom in-app update URLs | `UpdateRepositoryImpl` | Reset if application data is cleared | `app/src/main/java/com/gmail/nthai/data/repository/UpdateRepositoryImpl.kt#L25` |
| Local File Storage / Cache | Temporary staging for Excel reports and downloaded APK updates | `ExcelExporter.kt`, `ApkInstaller.kt` via `FileProvider` | Device storage exhaustion on large batch operations | `app/src/main/java/com/gmail/nthai/data/excel/ExcelExporter.kt` |

### 3) Secrets and Credentials Handling

- Credential sources: No third-party API keys, OAuth tokens, or client secrets are required. Cellular SMS transmission and phone calling rely directly on physical device SIM cards.
- Hardcoding checks: No secrets or private tokens are hardcoded. In-app update URL defaults to public repository endpoint: `https://raw.githubusercontent.com/HaiGH-Space/AutoSms/master/update.json`.
- Rotation or lifecycle notes: Not applicable; no private tokens exist in the codebase.

### 4) Reliability and Failure Behavior

- Retry/backoff behavior:
  - SMS: Managed by `ProcessSimSendingUseCase` and `SimSlotSendingController`. Failed sends capture carrier failure error codes via `SmsStatusReceiver`. Retries must respect randomized delay and quota boundaries.
  - Auto Call: Managed by `AutoCallEngine`. Respects configured minimum/maximum call interval delays, maximum call duration caps, and ringing timeouts.
  - Zalo: `ZaloSendingEngine` enforces a 15-second timeout waiting for the Accessibility event confirmation (`withTimeoutOrNull(15000L)`). `ZaloAccessibilityService` includes a send button click retry loop (`maxRetries = 3`), clipboard paste fallback, and low-spec device pacing.
- Timeout policy: Configured in `ZaloSendingEngine.kt` (15,000 ms), `AutoCallEngine.kt` call timeout, and standard Android `HttpURLConnection` connect/read timeouts for APK updates.
- Circuit-breaker or fallback behavior: When a SIM slot reaches its configured daily quota or operates outside the allowed hour/minute window, `ValidateSendingConstraintUseCase` pauses or halts sending for that slot.

### 5) Observability for Integrations

- Logging around external calls: Native Logcat output (`android.util.Log.d`, `Log.e`) logs dispatch steps, call state transitions, intent receiver events, accessibility node interactions, and return-to-app transitions.
- Metrics/tracing coverage: High-level metric counters recorded locally in `sending_logs`, `zalo_sending_logs`, and `call_logs` tables (`SENT`, `DELIVERED`, `FAILED`, `CANCELLED`, `COMPLETED`).
- Missing visibility gaps: No remote crash reporting (e.g. Firebase Crashlytics) or remote analytics are configured; failures are diagnosable only on physical devices via Logcat or exported Excel reports.

### 6) Evidence

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/gmail/nthai/service/AutoCallForegroundService.kt`
- `app/src/main/java/com/gmail/nthai/domain/engine/AutoCallEngine.kt`
- `app/src/main/java/com/gmail/nthai/service/ZaloAccessibilityService.kt`
- `app/src/main/java/com/gmail/nthai/service/ZaloSendingEngine.kt`
- `app/src/main/java/com/gmail/nthai/data/repository/UpdateRepositoryImpl.kt`
- `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt`
- `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt`
