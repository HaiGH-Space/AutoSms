# Codebase Structure

## Core Sections (Required)

### 1) Top-Level Map

| Path | Purpose | Evidence |
|------|---------|----------|
| `app/` | Android application module containing source code, AndroidManifest, resources, and unit/instrumented tests | `app/build.gradle.kts`, `app/src/main/` |
| `gradle/` | Gradle wrapper binaries, properties, and centralized dependency catalog (`libs.versions.toml`) | `gradle/libs.versions.toml`, `gradle/wrapper/` |
| `docs/` | Project documentation directory, including deep architecture and codebase knowledge maps (`codebase/`) and design specs (`spec/`) | `docs/codebase/`, `docs/spec/` |
| `.agents/` | AI agent operational rules, reference guides, and specialized development skills | `.agents/skills/` |
| `.github/` | GitHub Actions automated workflows (CI pipeline) | `.github/workflows/ci.yml` |
| `build.gradle.kts` | Root project Gradle build configuration declaring top-level plugins | `build.gradle.kts` |
| `settings.gradle.kts` | Defines project name (`AutoSms`) and included modules (`:app`) | `settings.gradle.kts` |
| `gradle.properties` | JVM configuration, daemon settings, and Android Gradle Plugin flags | `gradle.properties` |
| `DESIGN.md` | Design specification tokens and guidelines based on Vercel design language | `DESIGN.md` |
| `update.json` | Remote metadata endpoint for checking in-app application version updates (v0.2.1) | `update.json` |
| `docs/assets/logo.svg` | Vector brand mark and app icon in SVG format used across documentation and GitHub display | `docs/assets/logo.svg` |
| `README.md` | Stated project purpose, feature set, requirements, and user guide | `README.md` |

### 2) Entry Points

- Main runtime entry: `app/src/main/java/com/gmail/nthai/MainActivity.kt` (Single Activity launched via `android.intent.action.MAIN` / `android.intent.category.LAUNCHER`)
- Application context entry: `app/src/main/java/com/gmail/nthai/AutoSmsApplication.kt` (Annotated with `@HiltAndroidApp`, initializes logging workarounds and WorkManager configuration)
- Secondary background execution entry points:
  - `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt` (Foreground Service running SMS dispatch queue with `foregroundServiceType="dataSync"`)
  - `app/src/main/java/com/gmail/nthai/service/AutoCallForegroundService.kt` (Foreground Service executing automated voice calling queue with `foregroundServiceType="phoneCall"`)
  - `app/src/main/java/com/gmail/nthai/service/ZaloAccessibilityService.kt` (AccessibilityService interacting with the Zalo app UI, configured via `zalo_accessibility_config.xml`)
  - `app/src/main/java/com/gmail/nthai/service/SmsStatusReceiver.kt` (BroadcastReceiver catching `ACTION_SMS_SENT` and `ACTION_SMS_DELIVERED`)
  - `app/src/main/java/com/gmail/nthai/service/DailySmsWorker.kt` (Hilt-injected CoroutineWorker scheduled for daily SIM quota resets)
- How entry is selected: Declared explicitly in `app/src/main/AndroidManifest.xml`. UI begins at `MainActivity`, which renders `AppNavHost` in Jetpack Compose starting on `Screen.Home`.

### 3) Module Boundaries

| Boundary | What belongs here | What must not be here |
|----------|-------------------|------------------------|
| `data/` | Database implementations (Room DAOs, Entities, SQLite migrations v1-v15), Apache POI Excel parsing/exporting (`ExcelParser`, `ExcelExporter`), concrete Repository implementations (`CustomerRepositoryImpl`, `ZaloRepositoryImpl`, `AutoCallRepositoryImpl`, `CallRecipientRepositoryImpl`, `BlacklistRepositoryImpl`, `UpdateRepositoryImpl`), low-level SMS/call managers (`SmsSenderEngine`, `SimManagerWrapper`) | UI widgets, Compose composables, ViewModels, or direct user interaction logic |
| `domain/` | Pure business entities/models (`Customer`, `PendingImportState`, `ImportMode`, `ZaloRecipient`, `CallRecipient`, `BlacklistItem`, `AutoCallConfig`, `AutoCallState`), repository interfaces (`CustomerRepository`, `ZaloRepository`, `AutoCallRepository`, `CallRecipientRepository`, `BlacklistRepository`), domain engines (`SmsSendingEngine`, `AutoCallEngine`), telephony observers (`PhoneCallStateObserver`), and use cases (`ParseSpintaxUseCase`, `DetectVietnameseCarrierUseCase`, `ValidateSendingConstraintUseCase`, `ProcessSimSendingUseCase`) | Android framework UI imports, direct database access, network connections, or Activity/Service lifecycle code |
| `presentation/` | Jetpack Compose screens (`HomeScreen`, `CustomerListScreen`, `ZaloScreen`, `AutoCallScreen`, etc.), reusable UI components (`ExcelImportDialogs.kt`, `common/`, `customer/`, `home/`, `progress/`, `report/`, `zalo/`, `autocall/`, `config/`), navigation definitions (`AppNavHost`, `Screen`), themes, and ViewModels (`CustomerViewModel`, `ZaloViewModel`, `AutoCallViewModel`, `BlacklistViewModel`) | Raw database queries, low-level telephony calls, direct file system I/O, or POI spreadsheet parsing |
| `service/` | Android lifecycle services (`SmsForegroundService`, `AutoCallForegroundService`, `ZaloAccessibilityService`), background controllers (`SimSlotSendingController`, `ZaloSendingEngine`), notification helpers (`SmsNotificationHelper`), broadcast receivers (`SmsStatusReceiver`) | Direct UI rendering or business rule computations that belong in Domain use cases |
| `di/` | Hilt Dagger dependency injection modules (`DatabaseModule`, `RepositoryModule`, `SmsModule`, `AutoCallModule`, `UseCaseModule`, `UpdateModule`, `ZaloModule`) providing singletons and repository bindings | Application business logic, database queries, or UI state management |
| `util/` | Pure utility functions (Permission verification, phone number formatting & normalization, date formatting, format utils, APK package installer helpers, POI initializer) | Domain business logic, repository calls, or stateful service orchestration |

### 4) Naming and Organization Rules

- File naming pattern: PascalCase for Kotlin files and classes (`CustomerRepositoryImpl.kt`, `HomeScreen.kt`, `AutoCallScreen.kt`, `SmsForegroundService.kt`), snake_case for XML resources (`activity_main.xml`, `zalo_accessibility_config.xml`).
- Directory organization pattern: Layered Clean Architecture (`data`, `domain`, `presentation`, `service`, `di`, `util`) inside base package `com.gmail.nthai`.
- Presentation sub-organization: Grouped by functional component type:
  - `screens/`: Top-level screen composables (`HomeScreen`, `CustomerListScreen`, `ZaloScreen`, `AutoCallScreen`, `AutoSmsScreen`, `SendingProgressScreen`, `ReportScreen`, `SimAndRuleConfigScreen`, `TemplateEditorScreen`, `ManagementScreen`).
  - `components/`: Contains top-level shared dialogs like `ExcelImportDialogs.kt`, and modular sub-packages:
    - `common/`: Reusable primitives and shared campaign components (`AutoSmsCardContainer`, `AutoSmsSearchBar`, `AutoSmsTopAppBar`, `CampaignBottomActionBar`, `CampaignStatusBanner`, `CustomerListSharedComponents`, `FeatureCampaignScaffold`, `GenericCustomerItemCard`, `GenericFilterChipsBar`, `GenericProgressLogsContent`, `GenericRecipientListContent`, `GenericSelectionDialog`, `LogRecordCard`, `StatusBadge`, `SelectionOptionCard`).
    - `customer/`: `CustomerFilterSection.kt`, `CustomerSelectionBar.kt`, `CustomerEmptyState.kt`, `CustomerBottomSendingBar.kt`, `CustomerItemRow.kt`, `SimSlotTabs.kt`.
    - `home/`: `OverviewMetricsGrid.kt`, `ModernSimCard.kt`, `WorkflowStepCard.kt`.
    - `autocall/`: `AutoCallConfigCard.kt`, `AutoCallProgressCard.kt`, `CallConfigTab.kt`, `CallProgressTab.kt`, `CallRecipientTab.kt`.
    - `config/`: `BlacklistTab.kt`.
    - `progress/`: `ProgressSummaryCard.kt`, `SimSendingProgressCard.kt`.
    - `report/`: `ReportExportCard.kt`, `ReportFilterSection.kt`, `ReportMetricHeader.kt`.
    - `zalo/`: `ZaloAccessibilityStatusCard.kt`, `ZaloConfigTab.kt`, `ZaloMessageTemplateCard.kt`, `ZaloProgressTab.kt`, `ZaloRecipientTab.kt`, `ZaloSendingIntervalCard.kt`.
  - `viewmodel/`: MVI/MVVM ViewModels (`CustomerViewModel`, `ZaloViewModel`, `AutoCallViewModel`, `BlacklistViewModel`, `TemplateViewModel`, `ProgressViewModel`, `ReportViewModel`, `ConfigViewModel`, `HomeDashboardViewModel`, `UpdateViewModel`).
  - `navigation/`: `AppNavHost.kt`, `Screen.kt`.
- Import aliasing or path conventions: Explicit package imports matching Maven artifact namespaces or local domain paths; no wildcard imports.

### 5) Evidence

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/gmail/nthai/AutoSmsApplication.kt`
- `app/src/main/java/com/gmail/nthai/MainActivity.kt`
- `app/src/main/java/com/gmail/nthai/presentation/screens/AutoCallScreen.kt`
- `app/src/main/java/com/gmail/nthai/domain/engine/AutoCallEngine.kt`
- `app/src/main/java/com/gmail/nthai/domain/engine/SmsSendingEngine.kt`
- `app/src/main/java/com/gmail/nthai/service/AutoCallForegroundService.kt`
- `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt`
- `app/src/main/java/com/gmail/nthai/presentation/components/common/FeatureCampaignScaffold.kt`
- `app/src/main/java/com/gmail/nthai/presentation/navigation/AppNavHost.kt`
- `app/src/main/java/com/gmail/nthai/presentation/navigation/Screen.kt`
