# Codebase Concerns

## Core Sections (Required)

### 1) Top Risks (Prioritized)

| Severity | Concern                                                                          | Evidence                                                                                                                                  | Impact                                                                                                                                                                                                                                                                                                                          | Suggested action                                                                                                                            |
| -------- | -------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| Resolved | Missing Room database migration from v10 to v11 with active destructive fallback | `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt#L74-L112`, `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt#L29`      | Upgrades from v10 to v11/v12 previously risked triggering `fallbackToDestructiveMigration`. Fixed by implementing `MIGRATION_10_11` creating `zalo_recipients` and `zalo_sending_logs` tables and indices.                                                                                                                   | Implemented `MIGRATION_10_11` in `DatabaseModule.kt`.                                                                                        |
| Resolved | Missing older Room migrations (v2->v3, v3->v4)                                   | `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt#L24-L73`                                                                          | Any existing user jumping across older versions will have their database wiped                                                                                                                                                                                                                                                  | Resolved per Decision #2: Explicitly dropped backward compatibility for pre-release versions < v0.1.0 (< DB v4). Documented policy in `DatabaseModule.kt`. |
| Resolved | Brittle UI automation for Zalo via Accessibility Service                         | `app/src/main/java/com/gmail/nthai/service/ZaloAccessibilityService.kt`, `app/src/main/java/com/gmail/nthai/service/ZaloSendingEngine.kt` | UI automation across third-party Zalo updates is inherently non-standard without official API. Mitigated by multi-layer node search, clipboard paste fallback, WebView error detection, low-spec retry pacing, and conditional back loop. Resolved as Accepted/Mitigated per Decision #5. | Accepted technical limitation of Accessibility automation for personal Zalo. Hardening complete with fallback heuristics and low-spec device options. |
| Resolved | Apache POI memory consumption on Android                                         | `app/src/main/java/com/gmail/nthai/data/excel/ExcelParser.kt`, `app/src/main/java/com/gmail/nthai/data/excel/ExcelExporter.kt`, `app/src/main/java/com/gmail/nthai/util/PoiInitializer.kt` | Processing large `.xlsx` files previously loaded full DOM tree into RAM, causing heap spikes / OOM on low-RAM devices                                                                                                          | Implemented streaming SAX parser in `ExcelParser`, `SXSSFWorkbook` streaming writer in `ExcelExporter`, and `PoiInitializer`. |
| Resolved | Foreground Service runtime restrictions on Android 14+                           | `app/src/main/AndroidManifest.xml#L15-L16`, `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt`                           | Modern Android strictly regulates `dataSync` foreground service types and enforces execution time limits / BOOT start restrictions                                                                                                                                                                                              | Resolved per Decision #6: Manifest declares `FOREGROUND_SERVICE_DATA_SYNC` with `foregroundServiceType="dataSync"`, service launch is guarded with exception handling, notification dismissal on stop is cleanly enforced via `STOP_FOREGROUND_REMOVE`, and dispatch is user-initiated from UI. |

### 2) Technical Debt

List the most important debt items only.

| Debt item (Resolved)                              | Why it exists                                                                  | Where                                                             | Risk if ignored                                                                                                         | Suggested fix                                                                                       |
| ------------------------------------------------- | ------------------------------------------------------------------------------ | ----------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| Centralized POI Initializer                       | Apache POI expects full Java SE desktop logging; Android lacks standard Log4j2 | `app/src/main/java/com/gmail/nthai/util/PoiInitializer.kt`       | If POI is invoked before Application initialization or in isolated processes, app crashes with `ClassNotFoundException` | Resolved by centralizing POI initialization in `PoiInitializer` thread-safe singleton.              |
| [Resolved] TODO in production resource config     | Backup rules template left uncustomized                                        | `app/src/main/res/xml/data_extraction_rules.xml#L8`, `app/src/main/res/xml/backup_rules.xml` | App backups may unintentionally include or exclude sensitive local customer databases                                   | Explicitly excluded `auto_sms_database` files and included `sharedpref`.                            |
| [Resolved] No automated CI/CD pipeline            | Project relies on manual local builds                                          | `.github/workflows/ci.yml`                                       | Build regressions, lint failures, or broken tests may be pushed unnoticed                                               | Created GitHub Actions workflow running `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`. |
| [Resolved] Automated testing for Zalo module      | Rapid feature implementation in recent commits                                 | `app/src/test/java/com/gmail/nthai/`                              | Regressions in Zalo parsing, deep link generation, or repository logic go undetected                                    | Introduced unit tests covering Zalo entity/domain mappers, Spintax parsing, and deep link formatting. |

### 3) Security Concerns

| Risk | OWASP category (if applicable) | Evidence | Current mitigation | Gap | Resolution |
| --- | --- | --- | --- | --- | --- |
| Unencrypted customer PII in local SQLite database | M9: Insecure Data Storage | `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt` | Sandboxed Android app internal storage, excluded from backups/transfers (`data_extraction_rules.xml`, `backup_rules.xml`) | Physical forensic access or root access can extract data; SQLCipher overhead not justified for non-enterprise | **Resolved (Accepted/Mitigated per Decision #7)**: Protected by OS-level sandbox isolation and explicit cloud/device backup exclusions. |
| Accessibility Service exploitation risk | M1: Improper Platform Usage | `app/src/main/AndroidManifest.xml#L56-L68`, `app/src/main/res/xml/zalo_accessibility_config.xml` | Restricted target packages in XML (`android:packageNames="com.zing.zalo"`) and code validation; user enablement required in Settings | Accessibility services hold broad permissions; must remain strictly guarded | **Resolved (Hardened per Decision #8)**: Service is scoped at the OS level to `com.zing.zalo` in `zalo_accessibility_config.xml` and validates package in event handling before inspecting nodes. |
| Public cleartext HTTP fallback for in-app updates | M3: Insecure Communication | `app/src/main/java/com/gmail/nthai/data/repository/UpdateRepositoryImpl.kt`, `app/src/main/res/xml/network_security_config.xml` | App-wide Network Security Config disallows cleartext HTTP; `UpdateRepositoryImpl` enforces HTTPS protocol | Previously allowed arbitrary HTTP URLs or redirects | **Resolved per Decision #9**: Created `network_security_config.xml` disabling cleartext traffic app-wide, declared in Manifest, and enforced HTTPS-only on all update URLs, download URLs, and redirect targets in `UpdateRepositoryImpl`. |

### 4) Performance and Scaling Concerns

| Concern                                | Evidence                                                                | Current symptom                                              | Scaling risk                                                       | Suggested improvement                                                                                       |
| -------------------------------------- | ----------------------------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------- |
| [Resolved] Streaming POI parsing & export | `app/src/main/java/com/gmail/nthai/data/excel/ExcelParser.kt`        | None (previously memory spikes / freeze with large files)    | Out of memory crash when importing spreadsheets with >5,000 rows   | Resolved by SAX event parser in `ExcelParser` and `SXSSFWorkbook` streaming writer in `ExcelExporter`.     |
| [Resolved] Concurrent SIM sending lock contention | `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt#L88`, `app/src/main/java/com/gmail/nthai/domain/engine/SmsSendingEngine.kt` | None (previously synchronized blocks on locks) | Lock contention during simultaneous Dual SIM high-frequency events | Resolved by refactoring sending logic into `SmsSendingEngine` using lock-free atomic primitives (`AtomicInteger`) and thread-safe sets (`ConcurrentHashMap.newKeySet()`). |

### 5) Fragile/High-Churn Areas

| Area                                           | Why fragile                                                                                             | Churn signal          | Safe change strategy                                                                               |
| ---------------------------------------------- | ------------------------------------------------------------------------------------------------------- | --------------------- | -------------------------------------------------------------------------------------------------- |
| [Resolved] `CustomerListScreen.kt`             | Previously mixed filtering, SIM switching, empty state, selection, and sending bar                      | Monolithic (541 lines)| **Resolved**: Decomposed monolithic screen into dedicated stateless subcomponents (`CustomerFilterSection`, `CustomerSelectionBar`, `CustomerEmptyState`, `CustomerBottomSendingBar`, `SimSlotTabs`). Reduced to 273 lines. |
| [Resolved] `HomeScreen.kt`                     | Previously mixed metrics grid, SIM cards, and 4-step workflow cards in one screen                       | Monolithic (678 lines)| **Resolved**: Modularized into reusable stateless subcomponents (`OverviewMetricsGrid`, `ModernSimCard`, `WorkflowStepCard` in `presentation/components/home`). Reduced to 389 lines. |
| [Resolved] `app/build.gradle.kts`             | Frequent dependency bumps, Proguard rule adjustments, and packaging tweaks                              | Dependency churn      | **Resolved**: Centralized SDK versions (`compileSdk`, `minSdk`, `targetSdk`) in Version Catalog (`libs.versions.toml`) and created Gradle tasks `validateUpdateJson` & `syncUpdateJson`. |
| [Resolved] `update.json`                      | Frequently modified to trigger in-app updates across releases                                           | Config churn          | **Resolved**: Created Gradle sync/validation tasks and automated GitHub Actions release workflow (`.github/workflows/release.yml`) with base64 keystore signing. |
| [Resolved] `SmsForegroundService.kt` & `SmsSendingEngine.kt` | Coordinates multi-SIM state machines, notification updates, and coroutine background jobs               | Service complexity    | **Resolved**: Extracted SMS batch orchestration and multi-SIM state logic into testable `SmsSendingEngine.kt`, hardened `SmsForegroundService` for Android 14+ `dataSync` compliance and robust exception handling. |
| `CustomerViewModel.kt`                         | Customer state management, search, and Excel import bindings                                            | 12 commits in git log | Maintain isolated unit tests for ViewModel state reductions                                        |
| `ReportScreen.kt` & `SendingProgressScreen.kt` | Complex UI rendering for logs and progress counters                                                     | 12 commits in git log each | Separate reporting chart/card components into reusable stateless widgets                           |
| `DatabaseModule.kt` & `AppDatabase.kt`         | Active schema modifications (version 1 through 12 with `MIGRATION_10_11`)                                 | 11 commits in git log | Add automated `MigrationTestHelper` test suite before bumping Room versions                        |
| `ZaloConfigTab.kt` & `ZaloScreen.kt`           | Active development of Zalo campaign orchestration                                                       | 10 commits in git log | Decouple tab UI into stateless composables and inject testable engine states                       |
| `ZaloAccessibilityService.kt`                  | Hardening of accessibility nodes, return loops, and retry handlers                                      | 8 commits in git log  | Unit-test state transitions and navigation helpers where feasible; document Zalo UI node selectors |

### 6) `[ASK USER]` Decisions & Responses

1. **[RESOLVED] Should `MIGRATION_10_11` be created in `DatabaseModule.kt` immediately?**
   - **Decision:** **YES**. Implemented `MIGRATION_10_11` successfully to prevent destructive database wipes for existing users.
2. **[RESOLVED] Support legacy migrations prior to v4 (v2 to v4)?**
   - **Decision:** **NO (Drop)**. App explicitly drops backward compatibility for legacy versions prior to v0.1.0 (< DB v4).
3. **[RESOLVED] Setup CI/CD pipeline & static analysis?**
   - **Decision:** **YES**. Created GitHub Actions CI workflow (`.github/workflows/ci.yml`) and release automation workflow (`.github/workflows/release.yml`).
4. **[RESOLVED] Introduce unit tests for `ZaloSendingEngine` & `SmsSendingEngine`?**
   - **Decision:** **YES**. Introduced comprehensive unit test suites covering Zalo parsing, Spintax, and `SmsSendingEngine` behaviors.
5. **[RESOLVED] Brittle UI automation for Zalo via Accessibility Service policy?**
   - **Decision:** **Accepted / Mitigated**. Multi-tier hardening completed (multi-layer node detection, clipboard fallback, WebView error trap, retry cycles, and low-spec device pacing).
6. **[RESOLVED] Foreground Service runtime restrictions on Android 14+ (UPSIDE_DOWN_CAKE)?**
   - **Decision:** **Resolved / Compliant**. Configured `FOREGROUND_SERVICE_DATA_SYNC` permission and `foregroundServiceType="dataSync"`, with defensive exception handling and graceful teardown.
7. **[RESOLVED] Unencrypted customer PII in local SQLite database?**
   - **Decision:** **Accepted / Mitigated for Non-Enterprise**. OS-level sandbox isolation with explicit backup exclusions in `data_extraction_rules.xml` and `backup_rules.xml`.
8. **[RESOLVED] Accessibility Service exploitation risk?**
   - **Decision:** **Hardened / Resolved**. `zalo_accessibility_config.xml` explicitly scoped to `com.zing.zalo` at the OS level with runtime event validation.
9. **[RESOLVED] Insecure HTTP Communication for In-App Updates?**
   - **Decision:** **Resolved / Enforced HTTPS**. Network Security Config disallows cleartext HTTP; `UpdateRepositoryImpl` enforces HTTPS protocol across all URLs and redirects.

### 7) Evidence

- `docs/codebase/.codebase-scan.txt`
- `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt`
- `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt`
- `app/src/main/java/com/gmail/nthai/service/ZaloAccessibilityService.kt`
- `app/src/main/java/com/gmail/nthai/service/ZaloSendingEngine.kt`
- `app/src/main/java/com/gmail/nthai/domain/engine/SmsSendingEngine.kt`
- `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt`
- `app/src/main/java/com/gmail/nthai/data/excel/ExcelParser.kt`
- `app/src/main/java/com/gmail/nthai/data/excel/ExcelExporter.kt`
- `update.json`
- `.github/workflows/ci.yml`
- `.github/workflows/release.yml`
