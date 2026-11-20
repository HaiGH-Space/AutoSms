# Architecture

## Core Sections (Required)

### 1) Architectural Style

- Primary style: Clean Architecture with MVVM / MVI presentation layer, Domain Layer, and Data Layer.
- Why this classification:
  - Clear separation into three core layers: `presentation/` (ViewModels & Compose UI), `domain/` (pure business models, use cases, repository contracts), and `data/` (Room DB entities, DAOs, repositories, Excel parser, SMS sender).
  - Business rules (`ParseSpintaxUseCase`, `DetectVietnameseCarrierUseCase`, `ValidateSendingConstraintUseCase`, `ProcessSimSendingUseCase`, `CalculateNextDelayUseCase`) have zero dependencies on Android UI or framework database libraries.
- Primary constraints:
  - Android OS lifecycle boundaries and power management constraints (requires `ForegroundService` with `foregroundServiceType="dataSync"` and `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` for long-running batch SMS jobs).
  - Physical cellular hardware constraints (SIM slots 1 and 2 require quota tracking, carrier-matching rules, and randomized anti-spam delays).
  - Local device privacy constraint: all customer contacts, templates, and history remain strictly local on the device using SQLite/Room.
  - Third-party accessibility automation boundaries (`ZaloAccessibilityService` interacts with external `com.zing.zalo` application without public APIs).

### 2) System Flows

#### A. Excel Import & Column Matching Flow
```text
[Pick File URI] -> [ViewModel reads bytes & headers] -> [PendingImportState] -> [ExcelImportDialogs UI] -> [ExcelParser] -> [Room DB (Customer / ZaloRecipient)]
```
1. User selects `.xlsx` / `.xls` via file picker in `HomeScreen`, `CustomerListScreen`, or `ZaloScreen`.
2. ViewModel buffers file bytes and extracts header strings using `ImportCustomerExcelUseCase.getExcelHeaders`.
3. `PendingImportState` is emitted, displaying `ExcelImportDialogs` (`SelectPhoneColumnDialog`) where the user verifies the target phone header and selects `ImportMode` (`REPLACE_ALL`, `APPEND`, `SKIP_DUPLICATES`).
4. `ExcelParser` resolves columns using a 3-tier precedence: (1) exact match with selected header, (2) substring match, (3) fallback to common phone keywords (`sdt`, `sđt`, `phone`), and writes records into Room DB.

#### B. SMS Dispatch & Status Tracking Flow
```text
[Compose UI] -> [SmsForegroundService] -> [SimSlotSendingController] -> [ValidateSendingConstraintUseCase] -> [SmsSender] -> [SmsStatusReceiver] -> [Room DB Logs & StateFlow UI]
```
1. User starts sending from `SendingProgressScreen`. Intent action `ACTION_START` invokes `SmsForegroundService`.
2. A persistent notification is established (`CHANNEL_ID = "sms_service_channel"`).
3. `SimSlotSendingController` validates slot constraints (daily quota, allowed time window), calculates next delay via `CalculateNextDelayUseCase`, and calls `SmsSender.sendSms`.
4. `SmsStatusReceiver` captures system broadcasts `ACTION_SMS_SENT` and `ACTION_SMS_DELIVERED`, records log entries in `SendingLogDao`, and updates recipient status.
5. Room database emits reactive `Flow` updates consumed by ViewModels and rendered live in Compose.

#### C. Zalo Automation & Navigation Return Flow
```text
[ZaloScreen] -> [ZaloSendingEngine] -> [DeepLink: https://zalo.me/{phone}] -> [ZaloAccessibilityService] -> [Node Search / Paste / Send Click] -> [returnToAutoSms()] -> [AutoSms Foreground]
```
1. `ZaloSendingEngine` runs a coroutine loop iterating over `ZaloRecipient` entities.
2. It parses message spintax and triggers an Intent to open `https://zalo.me/{phone}`.
3. `ZaloAccessibilityService` (configured with `flagIncludeNotImportantViews`) listens for window events in `com.zing.zalo`:
   - Checks for WebView error dialogs or "not found" indicators -> emits `SendResult.NotFound`.
   - Locates chat input node, applies message text via arguments bundle with clipboard paste fallback.
   - Locates send button with retry loop -> emits `SendResult.Success`.
4. Return Navigation: `ZaloAccessibilityService.returnToAutoSms()` brings `MainActivity` to the front (`FLAG_ACTIVITY_REORDER_TO_FRONT`) and triggers a conditional back sequence that monitors the active window and stops immediately once AutoSms is visible.

### 3) Layer/Module Responsibilities

| Layer or module | Owns | Must not own | Evidence |
|-----------------|------|--------------|----------|
| `presentation` | Rendering UI screens, collecting user intents, holding ephemeral UI state in ViewModels, observing domain Flows | Direct SQLite querying, sending raw SMS messages, parsing Excel byte streams | `app/src/main/java/com/gmail/nthai/presentation/` |
| `domain` | Business rules, spintax parsing, carrier prefix detection, delay calculation algorithms, import state models (`PendingImportState`), repository interfaces | Android UI framework dependencies, Room annotations, Context references | `app/src/main/java/com/gmail/nthai/domain/` |
| `data` | Room database tables, DAOs, schema migrations, Excel file I/O via Apache POI (`ExcelParser`, `ExcelExporter`), telephony manager wrappers (`SmsSender`) | UI navigation decisions, rendering states, activity lifecycle events | `app/src/main/java/com/gmail/nthai/data/` |
| `service` | Android foreground service lifecycle, notification management, background queue coordination, Zalo accessibility automation | Business model calculation (delegated to domain use cases) | `app/src/main/java/com/gmail/nthai/service/` |
| `di` | Dependency graph construction, singleton lifecycle scoping, database and repository provisioning | Procedural business execution or UI handling | `app/src/main/java/com/gmail/nthai/di/` |

### 4) Reused Patterns

| Pattern | Where found | Why it exists |
|---------|-------------|---------------|
| Repository Pattern | `data/repository/*` & `domain/repository/*` | Decouples data persistence (Room DB, SharedPreferences, FileProvider) from business logic |
| Single-Use UseCase | `domain/usecase/*` (e.g. `ParseSpintaxUseCase`, `ImportCustomerExcelUseCase`) | Encapsulates specific domain calculations into testable, reusable units |
| Foreground Service | `service/SmsForegroundService.kt` | Guarantees Android process survival during long batch SMS sending operations |
| StateFlow / MVI State | `presentation/viewmodel/*` | Provides single source of truth and unidirectional data flow for Jetpack Compose |
| In-Memory Engine / Controller | `service/ZaloSendingEngine.kt`, `service/SimSlotSendingController.kt` | Manages active queue state, pause/resume mechanics, and inter-message delay timers |

### 5) Known Architectural Risks

- **Foreground Service Android 14/15 Constraints**: `SmsForegroundService` uses `foregroundServiceType="dataSync"`. Modern Android versions enforce strict timeout limits (typically 6 hours max runtime) and kill long-running `dataSync` services if not carefully bounded.
- **Accessibility Service Automation Fragility**: `ZaloAccessibilityService` interacts with an external, third-party closed-source application (`com.zing.zalo`) via accessibility node tree scraping (`findAccessibilityNodeInfosByText`, input view actions). Any internal UI modification in the Zalo Android app can break node discovery. Recent additions (WebView error detection, clipboard paste fallback, retry mechanisms, and low-spec device adaptations) substantially reduce freeze issues but remain sensitive to Zalo layout changes.
- **Destructive SQLite Fallback & Missing Migration 10->11**: `DatabaseModule.kt` configures `fallbackToDestructiveMigration(dropAllTables = true)`. While `MIGRATION_11_12` exists (adding min/maxDelaySec), `MIGRATION_10_11` is missing, as well as older migrations (v2->v3, v3->v4). Upgrading an existing app installation across missing migration steps drops all user tables and historical logs.

### 6) Evidence

- `app/src/main/java/com/gmail/nthai/presentation/screens/HomeScreen.kt`
- `app/src/main/java/com/gmail/nthai/presentation/components/ExcelImportDialogs.kt`
- `app/src/main/java/com/gmail/nthai/domain/model/PendingImportState.kt`
- `app/src/main/java/com/gmail/nthai/presentation/viewmodel/CustomerViewModel.kt`
- `app/src/main/java/com/gmail/nthai/presentation/viewmodel/ZaloViewModel.kt`
- `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt`
- `app/src/main/java/com/gmail/nthai/service/ZaloAccessibilityService.kt`
- `app/src/main/java/com/gmail/nthai/service/ZaloSendingEngine.kt`
- `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt`
