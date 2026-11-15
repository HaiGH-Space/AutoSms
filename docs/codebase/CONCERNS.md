# Codebase Concerns

## Core Sections (Required)

### 1) Top Risks (Prioritized)

| Severity | Concern | Evidence | Impact | Suggested action |
|----------|---------|----------|--------|------------------|
| High | Missing Room database migration from v10 to v11 with destructive fallback | `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt#L83-L85`, `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt#L29` | Upgrading an existing app installation from database v10 (version 0.1.5) to v11 (version 0.1.6) triggers `fallbackToDestructiveMigration`, wiping all user customers, templates, SIM configs, and logs | Implement `MIGRATION_10_11` in `DatabaseModule.kt` to create `zalo_recipients` and `zalo_sending_logs` tables without dropping the database |
| High | Missing older Room migrations (v2->v3, v3->v4) | `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt#L24-L73` | Any existing user jumping across older versions will have their database wiped | Define explicit migration steps or archive backward compatibility expectations |
| High | Brittle UI automation for Zalo via Accessibility Service | `app/src/main/java/com/gmail/nthai/service/ZaloAccessibilityService.kt#L70-L80`, `app/src/main/java/com/gmail/nthai/service/ZaloSendingEngine.kt#L108-L127` | Any layout, text change (e.g. "Kết bạn"), or view tree update in the official Zalo app will cause automation failure or indefinite timeout | Add fallback verification, retry mechanisms, and clear user warnings regarding third-party app updates |
| Medium | Apache POI memory consumption on Android | `app/build.gradle.kts#L88`, `app/src/main/java/com/gmail/nthai/AutoSmsApplication.kt#L15-L17` | Processing large `.xlsx` files with many rows/columns can cause large memory spikes or `OutOfMemoryError` on low-RAM mobile devices | Consider streaming parsers or limiting the maximum rows parsed at one time |
| Medium | Foreground Service runtime restrictions on Android 14+ | `app/src/main/AndroidManifest.xml#L13-L14`, `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt` | Modern Android strictly regulates `dataSync` foreground service types and enforces execution time limits | Review compliance with Google Play Store dataSync foreground service policies |

### 2) Technical Debt

List the most important debt items only.

| Debt item | Why it exists | Where | Risk if ignored | Suggested fix |
|-----------|---------------|-------|-----------------|---------------|
| Hardcoded Log4j workaround for POI | Apache POI expects full Java SE desktop logging; Android lacks standard Log4j2 | `app/src/main/java/com/gmail/nthai/AutoSmsApplication.kt#L15-L17` | If POI is invoked before Application initialization or in isolated processes, app crashes with `ClassNotFoundException` | Ensure POI initialization wrapper is centralized and resilient |
| TODO in production resource config | Backup rules template left uncustomized | `app/src/main/res/xml/data_extraction_rules.xml#L8` | App backups may unintentionally include or exclude sensitive local customer databases | Configure `<include>` and `<exclude>` rules explicitly for Room database files |
| No automated CI/CD pipeline | Project relies on manual local builds | Repository root (no `.github/workflows/`) | Build regressions, lint failures, or broken tests may be pushed unnoticed | Add GitHub Actions workflow running `./gradlew test` and `./gradlew assembleDebug` on pull requests |
| Zero automated testing for Zalo module | Rapid feature implementation in recent commits | `app/src/test/java/com/gmail/nthai/` | Regressions in Zalo parsing, deep link generation, or repository logic go undetected | Introduce unit tests for `ZaloSendingEngine` and repository mappers |

### 3) Security Concerns

| Risk | OWASP category (if applicable) | Evidence | Current mitigation | Gap |
|------|--------------------------------|----------|--------------------|-----|
| Unencrypted customer PII in local SQLite database | M9: Insecure Data Storage | `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt` | Sandboxed Android app internal storage | Rooted devices or physical forensic access can extract full customer names, phone numbers, and message history; consider SQLCipher if enterprise security is required |
| Accessibility Service exploitation risk | M1: Improper Platform Usage | `app/src/main/AndroidManifest.xml#L55-L65` | Requires explicit user enablement in Android Accessibility Settings | Accessibility services hold broad permissions to inspect screen content and dispatch gestures; must remain strictly guarded against unintended interactions |
| Public cleartext HTTP fallback for in-app updates | M3: Insecure Communication | `app/src/main/java/com/gmail/nthai/data/repository/UpdateRepositoryImpl.kt#L29` | Default URL uses HTTPS (`https://raw.githubusercontent.com/...`) | Custom update URL input allows arbitrary URLs without certificate pinning or checksum verification |

### 4) Performance and Scaling Concerns

| Concern | Evidence | Current symptom | Scaling risk | Suggested improvement |
|---------|----------|-----------------|-------------|-----------------------|
| In-memory POI Workbook DOM parsing | `app/src/main/java/com/gmail/nthai/data/excel/ExcelParser.kt` | Noticeable UI freeze / memory spike when opening large files | Out of memory crash when importing spreadsheets with >5,000 rows | Offload parsing to IO coroutine dispatcher with progress chunking, or use lightweight SAX/streaming parsers |
| Concurrent SIM sending lock contention | `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt#L83` | Synchronized blocks on `progressLock` and `failureCountLock` | Lock contention during simultaneous Dual SIM high-frequency events | Refactor to lock-free atomic primitives (`AtomicInteger`) or Mutex |

### 5) Fragile/High-Churn Areas

| Area | Why fragile | Churn signal | Safe change strategy |
|------|-------------|-------------|----------------------|
| `CustomerListScreen.kt` | 13 commits in 90 days; mixes customer filtering, Excel mode selection, reset dialogs, and pagination | 13 commits in git log | Decompose screen into smaller dedicated subcomponents; write Compose UI unit tests |
| `SmsForegroundService.kt` | 12 commits in 90 days; coordinates multi-SIM state machines, notification updates, and coroutine jobs | 12 commits in git log | Separate state machine logic from Android Service lifecycle into a standalone testable controller |
| `app/build.gradle.kts` | 12 commits in 90 days; frequent dependency bumps, Proguard rule adjustments, and packaging tweaks | 12 commits in git log | Rely on version catalog (`libs.versions.toml`) and maintain changelogs for packaging changes |
| `HomeScreen.kt` | 11 commits in 90 days; primary launcher surface connecting Excel import, navigation, and overview cards | 11 commits in git log | Keep composables stateless and delegate actions to ViewModel events |
| `DatabaseModule.kt` & `AppDatabase.kt` | 9 and 8 commits in 90 days; active schema modifications (version 1 through 11) | 9 commits in git log | Add automated `MigrationTestHelper` test suite before bumping Room versions |

### 6) `[ASK USER]` Questions

1. [ASK USER] Should `MIGRATION_10_11` be created immediately in `DatabaseModule.kt` to prevent destructive data loss when users upgrade to v0.1.6?
2. [ASK USER] The repository has recently added Zalo automation via `ZaloAccessibilityService`, but `README.md` only documents SMS automation from Excel. Should `README.md` and documentation be updated to include the Zalo automation feature, or is it intended to remain experimental/hidden for now?
3. [ASK USER] Are older database migrations prior to v4 (such as migrations from schema v2 to v4) still required to be supported, or is a destructive reset acceptable for legacy installs prior to v0.1.0?
4. [ASK USER] Should a CI/CD pipeline (such as GitHub Actions) and a code quality tool (like Detekt or Ktlint) be configured to validate builds and tests automatically on pull requests?

### 7) Evidence

- `docs/codebase/.codebase-scan.txt`
- `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt#L24-L86`
- `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt#L29`
- `app/src/main/java/com/gmail/nthai/service/ZaloAccessibilityService.kt`
- `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt`
- `app/src/main/java/com/gmail/nthai/data/excel/ExcelParser.kt`

