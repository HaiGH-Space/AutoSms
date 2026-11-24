# Architecture

## Core Sections (Required)

### 1) Architectural Style

- Primary style: Clean Architecture with MVVM / MVI presentation layer, Domain Layer, and Data Layer.
- Why this classification:
  - Clear separation into three core layers: `presentation/` (ViewModels & Compose UI), `domain/` (pure business models, use cases, repository contracts, domain engines), and `data/` (Room DB entities, DAOs, repositories, Excel streaming parser/exporter, SMS & Telephony manager wrappers).
  - Business rules (`ParseSpintaxUseCase`, `DetectVietnameseCarrierUseCase`, `ValidateSendingConstraintUseCase`, `ProcessSimSendingUseCase`, `CalculateNextDelayUseCase`, `SmsSendingEngine`, `AutoCallEngine`) have zero dependencies on Android UI framework classes.
- Primary constraints:
  - Android OS lifecycle boundaries and power management constraints:
    - SMS dispatching requires `ForegroundService` with `foregroundServiceType="dataSync"`.
    - Auto Call execution requires `ForegroundService` with `foregroundServiceType="phoneCall"` and `MANAGE_OWN_CALLS` / `CALL_PHONE` / `READ_CALL_LOG` permissions.
    - Battery optimization whitelisting (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) is required for uninterrupted dispatch.
  - Cellular hardware constraints (Dual SIM slot quota tracking, carrier matching, anti-spam delay intervals, auto-stop after 3 consecutive failures).
  - Blacklist blocking constraint: Recipient numbers enrolled in `BlacklistEntity` are automatically intercepted and skipped across SMS, Auto Call, and Zalo dispatching flows.
  - Local device privacy constraint: all customer contacts, call logs, SMS records, and templates are stored strictly local on the device using SQLite/Room (Schema v15).
  - Third-party accessibility automation boundaries (`ZaloAccessibilityService` interacts with external `com.zing.zalo` application without public APIs; requires runtime permission check before campaign initiation).

### 2) System Flows

#### A. Excel Import & Column Matching Flow
```text
[Pick File URI] -> [ViewModel reads bytes & headers] -> [PendingImportState] -> [ExcelImportDialogs UI] -> [ExcelParser (SAX Streaming)] -> [Room DB (Customer / ZaloRecipient / CallRecipient)]
```
1. User selects `.xlsx` / `.xls` via file picker in `HomeScreen`, `CustomerListScreen`, `ZaloScreen`, or `AutoCallScreen`.
2. ViewModel buffers file bytes and extracts header strings using `ImportCustomerExcelUseCase.getExcelHeaders`.
3. `PendingImportState` is emitted, displaying `ExcelImportDialogs` (`SelectPhoneColumnDialog`) where the user verifies the target phone header and selects `ImportMode` (`REPLACE_ALL`, `APPEND`, `SKIP_DUPLICATES`).
4. `ExcelParser` resolves columns using a 3-tier precedence: (1) exact match with selected header, (2) substring match, (3) fallback to common phone keywords (`sdt`, `sđt`, `phone`), utilizing streaming SAX parsing to preserve low memory overhead, and writes records into Room DB.

#### B. SMS Dispatch & Status Tracking Flow
```text
[Compose UI] -> [SmsForegroundService] -> [SmsSendingEngine & SimSlotSendingController] -> [Blacklist Check] -> [ValidateSendingConstraintUseCase] -> [SmsSenderEngine] -> [SmsStatusReceiver] -> [Room DB Logs & StateFlow UI]
```
1. User starts sending from `SendingProgressScreen`. Intent action `ACTION_START` invokes `SmsForegroundService`.
2. A persistent notification is established (`CHANNEL_ID = "sms_service_channel"`, `foregroundServiceType="dataSync"`).
3. `SmsSendingEngine` coordinates active SIM slots, checks blacklist numbers via `BlacklistRepository`, validates constraints (daily quota, allowed hours), calculates next delays via `CalculateNextDelayUseCase`, tracks failure counts (auto-stops after 3 consecutive failures), and dispatches messages via `SmsSenderEngine`.
4. `SmsStatusReceiver` captures system broadcasts `ACTION_SMS_SENT` and `ACTION_SMS_DELIVERED`, records log entries in `SendingLogDao`, and updates recipient status.
5. Room database emits reactive `Flow` updates consumed by ViewModels and rendered live in Compose.

#### C. Auto Call Campaign Flow
```text
[AutoCallScreen] -> [Call Permissions Check] -> [AutoCallForegroundService (phoneCall)] -> [AutoCallEngine] -> [Blacklist Check] -> [PhoneCallStateObserver (TelephonyManager + CallLog)] -> [Duration & Timeout Tracking] -> [CallLogDao & CallRecipientDao]
```
1. User configures call delay, call duration limit, and timeout in `AutoCallScreen` (Tab Cấu hình), checks `CALL_PHONE` and `READ_CALL_LOG` permissions, and taps "Bắt đầu gọi".
2. `AutoCallForegroundService` launches with `foregroundServiceType="phoneCall"`.
3. `AutoCallEngine` iterates over active recipients in `CallRecipientRepository`, verifies blacklist exclusion, initiates phone call intent via Telecom/Telephony manager.
4. `PhoneCallStateObserver` tracks call state (`IDLE`, `OFFHOOK`, `RINGING`) and monitors Android system call log to accurately extract call duration seconds.
5. Logs are recorded in `CallLogDao` and recipient state (`COMPLETED`, `FAILED`, `CANCELLED`) is updated reactively.

#### D. Zalo Automation & Navigation Return Flow
```text
[ZaloScreen] -> [Accessibility Permission Check] -> [ZaloSendingEngine] -> [Blacklist Check] -> [DeepLink: https://zalo.me/{phone}] -> [ZaloAccessibilityService] -> [Node Search / Paste / Send Click] -> [returnToAutoSms()] -> [AutoSms Foreground]
```
1. `ZaloScreen` verifies that `ZaloAccessibilityService` is active before permitting campaign launch.
2. `ZaloSendingEngine` iterates over `ZaloRecipient` entities, verifies blacklist exclusion, parses message spintax, and triggers an Intent to open `https://zalo.me/{phone}`.
3. `ZaloAccessibilityService` (scoped to `com.zing.zalo` with `flagIncludeNotImportantViews`) listens for window events in Zalo:
   - Checks for WebView error dialogs or "not found" indicators -> emits `SendResult.NotFound`.
   - Locates chat input node, applies message text via arguments bundle with clipboard paste fallback.
   - Locates send button with retry loop -> emits `SendResult.Success`.
4. Return Navigation: `ZaloAccessibilityService.returnToAutoSms()` brings `MainActivity` to the front (`FLAG_ACTIVITY_REORDER_TO_FRONT`) and triggers a conditional back sequence that monitors the active window and stops immediately once AutoSms is visible.

### 3) Layer/Module Responsibilities

| Layer or module | Owns | Must not own | Evidence |
|-----------------|------|--------------|----------|
| `presentation` | Rendering UI screens, collecting user intents, holding ephemeral UI state in ViewModels, observing domain Flows | Direct SQLite querying, sending raw SMS messages, making telephony calls, parsing Excel byte streams | `app/src/main/java/com/gmail/nthai/presentation/` |
| `domain` | Business rules, spintax parsing, carrier prefix detection, delay calculation algorithms, SMS sending engine (`SmsSendingEngine`), auto call engine (`AutoCallEngine`), call state observer (`PhoneCallStateObserver`), blacklist repository interfaces | Android UI framework dependencies, Room annotations, Context references | `app/src/main/java/com/gmail/nthai/domain/` |
| `data` | Room database tables (Entities v15), DAOs, schema migrations (v1-v15), Excel streaming file I/O via Apache POI (`ExcelParser`, `ExcelExporter`), telephony manager wrappers (`SmsSenderEngine`, `SimManagerWrapper`) | UI navigation decisions, rendering states, activity lifecycle events | `app/src/main/java/com/gmail/nthai/data/` |
| `service` | Android foreground service lifecycle (`SmsForegroundService`, `AutoCallForegroundService`), notification management, background queue coordination, Zalo accessibility automation (`ZaloAccessibilityService`) | Business model calculation (delegated to domain use cases and engines) | `app/src/main/java/com/gmail/nthai/service/` |
| `di` | Dependency graph construction, singleton lifecycle scoping, database, repository, and engine provisioning | Procedural business execution or UI handling | `app/src/main/java/com/gmail/nthai/di/` |

### 4) Reused Patterns

| Pattern | Where found | Why it exists |
|---------|-------------|---------------|
| Repository Pattern | `data/repository/*` & `domain/repository/*` | Decouples data persistence (Room DB, SharedPreferences, FileProvider) from business logic |
| Single-Use UseCase | `domain/usecase/*` (e.g. `ParseSpintaxUseCase`, `DetectVietnameseCarrierUseCase`, `ProcessSimSendingUseCase`) | Encapsulates specific domain calculations into testable, reusable units |
| Foreground Service | `service/SmsForegroundService.kt`, `service/AutoCallForegroundService.kt` | Guarantees Android process survival during long batch SMS sending and Auto Call campaigns |
| Shared Feature Scaffold | `presentation/components/common/FeatureCampaignScaffold.kt`, `CampaignBottomActionBar.kt` | Unifies tab layouts (Danh sách, Cấu hình, Tiến độ), action bars, and status banners across SMS, Auto Call, and Zalo campaigns adhering to `DESIGN.md` |
| StateFlow / MVI State | `presentation/viewmodel/*`, `presentation/screens/AutoCallViewModel.kt` | Provides single source of truth and unidirectional data flow for Jetpack Compose |
| In-Memory Engine / Controller | `domain/engine/SmsSendingEngine.kt`, `domain/engine/AutoCallEngine.kt`, `service/ZaloSendingEngine.kt` | Manages active queue state, pause/resume mechanics, and inter-action delay timers |

### 5) Known Architectural Risks

- **Foreground Service Android 14/15 Constraints**: `SmsForegroundService` uses `foregroundServiceType="dataSync"` and `AutoCallForegroundService` uses `foregroundServiceType="phoneCall"`. Android 14+ strictly monitors foreground service types and enforces runtime timeout limits.
- **Accessibility Service Automation Fragility**: `ZaloAccessibilityService` interacts with an external, third-party closed-source application (`com.zing.zalo`) via accessibility node tree scraping (`findAccessibilityNodeInfosByText`, input view actions). Any internal UI modification in the Zalo Android app can break node discovery. Mitigated by multi-layer node search, clipboard paste fallback, WebView error detection, and low-spec retry pacing.
- **Destructive SQLite Fallback for Legacy Versions**: While migrations `MIGRATION_10_11` through `MIGRATION_14_15` are fully implemented and verified, versions prior to v0.1.0 (< DB v4) intentionally fall back to destructive migration if upgraded directly.

### 6) Evidence

- `app/src/main/java/com/gmail/nthai/presentation/screens/HomeScreen.kt`
- `app/src/main/java/com/gmail/nthai/presentation/screens/AutoCallScreen.kt`
- `app/src/main/java/com/gmail/nthai/domain/engine/AutoCallEngine.kt`
- `app/src/main/java/com/gmail/nthai/domain/engine/SmsSendingEngine.kt`
- `app/src/main/java/com/gmail/nthai/domain/telephony/PhoneCallStateObserver.kt`
- `app/src/main/java/com/gmail/nthai/presentation/components/common/FeatureCampaignScaffold.kt`
- `app/src/main/java/com/gmail/nthai/service/AutoCallForegroundService.kt`
- `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt`
- `app/src/main/java/com/gmail/nthai/service/ZaloAccessibilityService.kt`
- `app/src/main/java/com/gmail/nthai/data/local/AppDatabase.kt`
- `app/src/main/java/com/gmail/nthai/di/DatabaseModule.kt`
