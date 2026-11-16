# Architecture

## Core Sections (Required)

### 1) Architectural Style

- Primary style: Clean Architecture with MVVM / MVI presentation layer, Domain Layer, and Data Layer.
- Why this classification:
  - Clear separation into three core layers: `presentation/` (ViewModels & Compose UI), `domain/` (pure business models, use cases, repository contracts), and `data/` (Room DB entities, DAOs, repositories, Excel parser, SMS sender).
  - Business rules (`ParseSpintaxUseCase`, `DetectVietnameseCarrierUseCase`, `ValidateSendingConstraintUseCase`, `ProcessSimSendingUseCase`) have zero dependencies on Android UI or framework database libraries.
- Primary constraints:
  - Android OS lifecycle boundaries and power management constraints (requires `ForegroundService` and `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` for long-running batch SMS jobs).
  - Physical cellular hardware constraints (SIM slots 1 and 2 require quota tracking, carrier-matching rules, and randomized anti-spam delays).
  - Local device privacy constraint: all customer data, templates, and history remain strictly local on the device using SQLite/Room.

### 2) System Flow

```text
[Excel File / UI Input] -> [ExcelParser / ViewModel] -> [Domain UseCases / Repositories] -> [Room DB / SmsForegroundService] -> [SmsManager / SmsStatusReceiver]
```

1. **Import & Ingestion**: The user picks an `.xlsx`/`.xls` spreadsheet via `HomeScreen`. `CustomerViewModel` invokes `ExcelParser` (Apache POI), dynamically extracting columns and phone numbers, and stores customer entities in Room DB with status `PENDING` (`app/src/main/java/com/gmail/nthai/data/excel/ExcelParser.kt`).
2. **Template & Rule Preparation**: The user authors message templates containing Spintax syntax and placeholders. `TemplateViewModel` and `ParseSpintaxUseCase` evaluate dynamic tokens (`{Ten_KH}`, `{So_Tien}`, etc.) and randomize variations (`app/src/main/java/com/gmail/nthai/domain/usecase/ParseSpintaxUseCase.kt`).
3. **Dispatch Initialization**: The user initiates batch sending. `SendingProgressScreen` signals `SmsForegroundService` via intent action `ACTION_START`. The service creates a persistent notification to prevent OS process killing (`app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt#L96-L103`).
4. **Sending Execution Loop**: `SmsForegroundService` dispatches tasks through `SimSlotSendingController`. For each pending customer, it invokes `ValidateSendingConstraintUseCase` (checking daily quota and allowed sending time window) and `CalculateNextDelayUseCase` (injecting randomized delay), then triggers `SmsSender` (`app/src/main/java/com/gmail/nthai/domain/usecase/ProcessSimSendingUseCase.kt`).
5. **Telephony & Asynchronous Status Capture**: `SmsManager` sends single or multipart SMS messages with `PendingIntent` callbacks. `SmsStatusReceiver` captures `ACTION_SMS_SENT` and `ACTION_SMS_DELIVERED`, records the log in `SendingLogDao`, updates the customer's status, and notifies `SmsForegroundService.reportSmsFailure()` if failed (`app/src/main/java/com/gmail/nthai/service/SmsStatusReceiver.kt`).
6. **Reactive UI Updates**: The Room database emits updated records via Kotlin Coroutine `Flow`. ViewModels transform the stream into `StateFlow`, causing Jetpack Compose screens (`SendingProgressScreen`, `HomeScreen`) to re-render in real time.

### 3) Layer/Module Responsibilities

| Layer or module | Owns | Must not own | Evidence |
|-----------------|------|--------------|----------|
| `presentation` | Rendering UI screens, collecting user intents, holding ephemeral UI state in ViewModels, observing domain Flows | Direct SQLite querying, sending raw SMS messages, parsing Excel byte streams | `app/src/main/java/com/gmail/nthai/presentation/` |
| `domain` | Business rules, spintax parsing, carrier prefix detection, delay calculation algorithms, repository interfaces | Android UI framework dependencies, Room annotations, Context references | `app/src/main/java/com/gmail/nthai/domain/` |
| `data` | Room database tables, DAOs, schema migrations, Excel file I/O via Apache POI, telephony manager wrappers | UI navigation decisions, rendering states, activity lifecycle events | `app/src/main/java/com/gmail/nthai/data/` |
| `service` | Android foreground service lifecycle, notification management, background queue coordination, Zalo accessibility automation | Business model calculation (delegated to domain use cases) | `app/src/main/java/com/gmail/nthai/service/` |
| `di` | Dependency graph construction, singleton lifecycle scoping, database and repository provisioning | Procedural business execution or UI handling | `app/src/main/java/com/gmail/nthai/di/` |

### 4) Reused Patterns

| Pattern | Where found | Why it exists |
|---------|-------------|---------------|
| Repository Pattern | `data/repository/*` & `domain/repository/*` | Decouples data sources (Room DB, SharedPreferences, FileProvider) from business logic |
| Single-Use UseCase | `domain/usecase/*` (e.g. `ParseSpintaxUseCase`) | Encapsulates specific domain calculations into testable, reusable units |
| Foreground Service | `service/SmsForegroundService.kt` | Guarantees Android process survival during long batch sending operations |
| StateFlow / MVI State | `presentation/viewmodel/*` | Provides single source of truth and unidirectional data flow for Jetpack Compose |
| In-Memory Engine / Controller | `service/ZaloSendingEngine.kt`, `service/SimSlotSendingController.kt` | Manages active queue state, pause/resume mechanics, and inter-message delay timers |

### 5) Known Architectural Risks

- **Foreground Service Android 14/15 Constraints**: `SmsForegroundService` uses `foregroundServiceType="dataSync"`. Modern Android versions enforce strict timeout limits (typically 6 hours max runtime) and kill long-running `dataSync` services if not carefully bounded.
- **Accessibility Service Brittle Automation**: `ZaloAccessibilityService` interacts with an external, third-party closed-source application (`com.zing.zalo`) via accessibility node tree scraping (`findAccessibilityNodeInfosByText("Kết bạn")`, input view actions). Any internal UI modification in the Zalo Android app breaks the automation flow without compile-time warning. Recent improvements (WebView error detection, clipboard paste fallback, retry mechanisms, and low-spec device adaptations) substantially reduce freeze issues but remain sensitive to Zalo layout changes.
- **Destructive SQLite Fallback**: `DatabaseModule.kt` configures `fallbackToDestructiveMigration(dropAllTables = true)`. Because migration `10 -> 11` is missing (despite `11 -> 12` being added), upgrading from database schema v10 drops all user tables and historical logs.

### 6) Evidence

- `app/src/main/java/com/gmail/nthai/presentation/screens/HomeScreen.kt`
- `app/src/main/java/com/gmail/nthai/presentation/viewmodel/CustomerViewModel.kt`
- `app/src/main/java/com/gmail/nthai/domain/usecase/ParseSpintaxUseCase.kt`
- `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt`
- `app/src/main/java/com/gmail/nthai/service/SmsStatusReceiver.kt`
- `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt`

