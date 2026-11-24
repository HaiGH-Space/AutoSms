# Codebase Concerns

## Core Sections (Required)

### 1) Top Risks (Prioritized)

| Severity | Concern | Evidence | Impact | Suggested action | Status |
| -------- | ------- | -------- | ------ | ---------------- | ------ |
| Low | Brittle UI automation for Zalo via Accessibility Service | `app/src/main/java/com/gmail/nthai/service/ZaloAccessibilityService.kt`, `app/src/main/java/com/gmail/nthai/service/ZaloSendingEngine.kt` | UI automation across third-party Zalo updates is non-standard without official API. Mitigated by multi-layer node search, clipboard paste fallback, WebView error detection, low-spec retry pacing, and conditional back loop. | Accepted technical limitation of Accessibility automation for personal Zalo. Hardening complete with fallback heuristics and low-spec device options. | Resolved (Accepted / Mitigated) |
| Low | Foreground Service runtime restrictions on Android 14+ | `app/src/main/AndroidManifest.xml#L17-L20`, `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt`, `app/src/main/java/com/gmail/nthai/service/AutoCallForegroundService.kt` | Modern Android strictly regulates `dataSync` and `phoneCall` foreground service types and enforces execution time limits. | Manifest declares `FOREGROUND_SERVICE_DATA_SYNC` and `FOREGROUND_SERVICE_PHONE_CALL`, service launch is guarded with exception handling, notification dismissal on stop is cleanly enforced via `STOP_FOREGROUND_REMOVE`, and dispatch is user-initiated from UI. | Resolved |
| Low | Missing older Room migrations (< DB v4) | `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt#L24-L73` | Any existing user jumping across older versions (< v0.1.0) will have their database wiped due to `fallbackToDestructiveMigration`. | Explicitly dropped backward compatibility for pre-release versions < v0.1.0 (< DB v4). Documented policy in `DatabaseModule.kt`. | Resolved (Policy Decision) |
| Resolved | Missing Room database migration from v10 to v15 | `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt`, `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt` | Upgrades across versions previously risked triggering destructive migration. Fixed by implementing `MIGRATION_10_11` through `MIGRATION_14_15`. | Implemented and verified migrations through `DatabaseMigrationTest.kt`. | Resolved |
| Resolved | Apache POI memory consumption on Android | `app/src/main/java/com/gmail/nthai/data/excel/ExcelParser.kt`, `app/src/main/java/com/gmail/nthai/data/excel/ExcelExporter.kt` | Large `.xlsx` files previously loaded full DOM tree into RAM, causing heap spikes / OOM. | Implemented streaming SAX parser in `ExcelParser`, `SXSSFWorkbook` streaming writer in `ExcelExporter`, and `PoiInitializer`. | Resolved |

### 2) Technical Debt

List the most important debt items only.

| Debt item | Why it exists | Where | Risk if ignored | Status / Resolution |
| --------- | ------------- | ----- | --------------- | ------------------- |
| Automated Release Workflow | Release workflow `.github/workflows/release.yml` was deleted in commit `eccf217` | `.github/workflows/` | Releases must currently be tagged/published manually without automated GitHub Actions release build | [TODO] Re-enable and configure automated release workflow when signing secrets are verified. |
| Test Code Coverage Tooling | No coverage reporting tool (JaCoCo / Kover) | `build.gradle.kts`, `app/build.gradle.kts` | Untracked regression in unit test coverage as new features are added | [TODO] Add JaCoCo or Kover plugin to build configuration. |
| Backup Rules Configuration | Backup rules configuration | `app/src/main/res/xml/data_extraction_rules.xml`, `app/src/main/res/xml/backup_rules.xml` | App backups may unintentionally include local databases | Resolved: Explicitly excluded `auto_sms_database` files and included `sharedpref`. |
| Centralized POI Initializer | POI logging initialization | `app/src/main/java/com/gmail/nthai/util/PoiInitializer.kt` | Missing initialization causes crashes | Resolved by centralizing POI initialization in `PoiInitializer` thread-safe singleton. |

### 3) Security Concerns

| Risk | OWASP category | Evidence | Current mitigation | Gap | Resolution |
| --- | --- | --- | --- | --- | --- |
| Unencrypted customer PII & Call logs in local SQLite database | M9: Insecure Data Storage | `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt` | Sandboxed Android app internal storage, excluded from backups/transfers (`data_extraction_rules.xml`, `backup_rules.xml`) | Physical forensic access or root access can extract data; SQLCipher overhead not justified for non-enterprise | **Resolved (Accepted/Mitigated)**: Protected by OS-level sandbox isolation and explicit cloud/device backup exclusions. |
| Accessibility Service exploitation risk | M1: Improper Platform Usage | `app/src/main/AndroidManifest.xml#L67-L77`, `app/src/main/res/xml/zalo_accessibility_config.xml` | Restricted target packages in XML (`android:packageNames="com.zing.zalo"`) and code validation; user enablement required in Settings | Accessibility services hold broad permissions; must remain strictly guarded | **Resolved (Hardened)**: Service is scoped at the OS level to `com.zing.zalo` in `zalo_accessibility_config.xml` and validates package in event handling before inspecting nodes. |
| Public cleartext HTTP fallback for in-app updates | M3: Insecure Communication | `app/src/main/java/com/gmail/nthai/data/repository/UpdateRepositoryImpl.kt`, `app/src/main/res/xml/network_security_config.xml` | App-wide Network Security Config disallows cleartext HTTP; `UpdateRepositoryImpl` enforces HTTPS protocol | Previously allowed arbitrary HTTP URLs or redirects | **Resolved**: Created `network_security_config.xml` disabling cleartext traffic app-wide, declared in Manifest, and enforced HTTPS-only on all update URLs, download URLs, and redirect targets. |

### 4) Performance and Scaling Concerns

| Concern | Evidence | Current symptom | Scaling risk | Status / Suggested improvement |
| ------- | -------- | --------------- | ------------ | ----------------------------- |
| Streaming POI parsing & export | `app/src/main/java/com/gmail/nthai/data/excel/ExcelParser.kt` | None | Out of memory crash when importing spreadsheets with >5,000 rows | Resolved: SAX event parser in `ExcelParser` and `SXSSFWorkbook` streaming writer in `ExcelExporter`. |
| Concurrent SIM sending lock contention | `app/src/main/java/com/gmail/nthai/domain/engine/SmsSendingEngine.kt` | None | Lock contention during simultaneous Dual SIM events | Resolved: Refactored sending logic into `SmsSendingEngine` using atomic primitives (`AtomicInteger`) and thread-safe sets (`ConcurrentHashMap.newKeySet()`). |

### 5) Fragile/High-Churn Areas

| Area | Why fragile | Churn signal | Status / Safe change strategy |
| ---- | ----------- | ------------ | ----------------------------- |
| `app/build.gradle.kts` | Version bumps, SDK updates, packaging configuration | 20 commits in git log | Centralized SDK versions (`compileSdk`, `minSdk`, `targetSdk`) in `libs.versions.toml`; validation tasks `validateUpdateJson` & `syncUpdateJson`. |
| `CustomerListScreen.kt` | Customer search, filter, selection, and dispatch UI | 22 commits in git log | Decomposed into dedicated modular components (`CustomerFilterSection`, `CustomerSelectionBar`, `CustomerEmptyState`, `CustomerBottomSendingBar`, `SimSlotTabs`). |
| `HomeScreen.kt` | Dashboard metrics, campaign shortcuts, and workflow cards | 19 commits in git log | Decomposed into modular components (`OverviewMetricsGrid`, `ModernSimCard`, `WorkflowStepCard`). |
| `AutoCallScreen.kt` & `AutoCallEngine.kt` | Automated voice calling campaign and state observer | Recent commits | Built with modular campaign components (`FeatureCampaignScaffold`, `CallRecipientTab`, `CallConfigTab`, `CallProgressTab`) and covered by `AutoCallEngineTest.kt`. |
| `ZaloScreen.kt` & `ZaloConfigTab.kt` | Zalo campaign configuration and accessibility handling | 13 commits in git log each | Modularized cards (`ZaloAccessibilityStatusCard`, `ZaloMessageTemplateCard`, `ZaloSendingIntervalCard`). |
| `DatabaseModule.kt` & `AppDatabase.kt` | Schema migrations and entity management | 17 commits in git log | Migrations tested via `DatabaseMigrationTest.kt` verifying v1 -> v15. |

### 6) `[ASK USER]` Decisions & Responses

1. **[RESOLVED] Should `MIGRATION_10_11` through `MIGRATION_14_15` be created in `DatabaseModule.kt` immediately?**
   - **Decision:** **YES**. Implemented migrations up to v15 to support Blacklist, CallLogs, and CallRecipients without data loss.
2. **[RESOLVED] Support legacy migrations prior to v4 (v2 to v4)?**
   - **Decision:** **NO (Drop)**. App explicitly drops backward compatibility for legacy versions prior to v0.1.0 (< DB v4).
3. **[RESOLVED] Setup CI/CD pipeline & static analysis?**
   - **Decision:** **YES**. Created GitHub Actions CI workflow (`.github/workflows/ci.yml`).
4. **[RESOLVED] Introduce unit tests for `ZaloSendingEngine`, `SmsSendingEngine` & `AutoCallEngine`?**
   - **Decision:** **YES**. Comprehensive unit test suites created and verified.
5. **[RESOLVED] Brittle UI automation for Zalo via Accessibility Service policy?**
   - **Decision:** **Accepted / Mitigated**. Multi-tier hardening completed.
6. **[RESOLVED] Foreground Service runtime restrictions on Android 14+ (UPSIDE_DOWN_CAKE)?**
   - **Decision:** **Resolved / Compliant**. Configured `FOREGROUND_SERVICE_DATA_SYNC` and `FOREGROUND_SERVICE_PHONE_CALL`, with defensive exception handling and graceful teardown.
7. **[RESOLVED] Unencrypted customer PII in local SQLite database?**
   - **Decision:** **Accepted / Mitigated for Non-Enterprise**. OS-level sandbox isolation with explicit backup exclusions in `data_extraction_rules.xml` and `backup_rules.xml`.
8. **[RESOLVED] Accessibility Service exploitation risk?**
   - **Decision:** **Hardened / Resolved**. `zalo_accessibility_config.xml` explicitly scoped to `com.zing.zalo` at the OS level.
9. **[RESOLVED] Insecure HTTP Communication for In-App Updates?**
   - **Decision:** **Resolved / Enforced HTTPS**. Network Security Config disallows cleartext HTTP; `UpdateRepositoryImpl` enforces HTTPS protocol across all URLs and redirects.

### 7) Evidence

- `docs/codebase/.codebase-scan.txt`
- `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt`
- `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt`
- `app/src/main/java/com/gmail/nthai/service/AutoCallForegroundService.kt`
- `app/src/main/java/com/gmail/nthai/domain/engine/AutoCallEngine.kt`
- `app/src/main/java/com/gmail/nthai/service/ZaloAccessibilityService.kt`
- `app/src/main/java/com/gmail/nthai/service/ZaloSendingEngine.kt`
- `app/src/main/java/com/gmail/nthai/domain/engine/SmsSendingEngine.kt`
- `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt`
- `update.json`
- `.github/workflows/ci.yml`
