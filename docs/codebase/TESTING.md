# Testing Patterns

## Core Sections (Required)

### 1) Test Stack and Commands

- Primary test framework: JUnit 4 (`junit:junit:4.13.2`) for JVM unit tests; AndroidX JUnit (`androidx.test.ext:junit:1.2.1`) for instrumented tests.
- Assertion/mocking tools: Standard `org.junit.Assert` (`assertEquals`, `assertTrue`, `assertNotNull`, `fail`), `kotlinx.coroutines.test` for testing coroutine dispatches. Tests rely on real in-memory data structures, lightweight test fakes (`FakeBlacklistRepository`), and deterministic seeds.
- Commands:

```bash
# Run all JVM unit tests
./gradlew test

# Run unit tests specifically for the app module
./gradlew :app:testDebugUnitTest

# Validate update.json version alignment with build.gradle.kts
./gradlew :app:validateUpdateJson

# Run instrumented UI and navigation tests on connected Android device/emulator
./gradlew connectedAndroidTest
```

### 2) Test Layout

- Test file placement pattern:
  - Unit tests: `app/src/test/java/com/gmail/nthai/`
  - Instrumented UI/Compose tests: `app/src/androidTest/java/com/gmail/nthai/`
- Naming convention: SourceClassName + `Test.kt` (e.g. `ParseSpintaxUseCaseTest.kt`, `ExcelParserTest.kt`, `ProcessSimSendingUseCaseTest.kt`, `SmsSendingEngineTest.kt`, `AutoCallEngineTest.kt`, `DatabaseMigrationTest.kt`, `ZaloAccessibilityServiceTest.kt`, `CustomerViewModelTest.kt`, `PhoneNumberUtilsTest.kt`, `AppNavigationTest.kt`).
- Setup files and where they run: Instrumented tests utilize `androidx.test.runner.AndroidJUnitRunner` declared as `testInstrumentationRunner` in `app/build.gradle.kts#L19`.

### 3) Test Scope Matrix

| Scope | Covered? | Typical target | Notes |
|-------|----------|----------------|-------|
| Unit | Yes | Domain UseCases (`ParseSpintaxUseCase`, `DetectVietnameseCarrierUseCase`, `CalculateNextDelayUseCase`, `ValidateSendingConstraintUseCase`, `ProcessSimSendingUseCase`), Domain Engines (`SmsSendingEngineTest`, `AutoCallEngineTest`), ViewModels (`CustomerViewModelTest`), Mappers (`ZaloMapperTest`), Accessibility & Service logic (`ZaloAccessibilityServiceTest`, `ZaloSpintaxAndDeeplinkTest`), Database Migrations (`DatabaseMigrationTest` up to v15), Utilities (`PhoneNumberUtilsTest`), and `ExcelParserTest` | High coverage over business logic, regex evaluation, database schema migration (v1->v15), Spintax, phone formatting, blacklist logic, and Excel streaming. |
| Integration | Yes | Room DAOs and database migrations (`DatabaseMigrationTest`) | Tests SQLite migration paths v1 -> v15 using real schema scripts and Room in-memory instances. |
| E2E / UI | Partial | `AppNavigationTest.kt` in `androidTest` | Validates Compose navigation graph transitions via `androidx.compose.ui.test.junit4`. |

### 4) Mocking and Isolation Strategy

- Main mocking approach: In-memory test fixtures, stateful fakes, and deterministic seeds.
  - `ParseSpintaxUseCaseTest` injects deterministic `kotlin.random.Random(seed)` instances to verify reproducible Spintax branch selection.
  - `ExcelParserTest` uses in-memory `ByteArrayOutputStream` with POI `XSSFWorkbook` to construct real binary Excel spreadsheets on the fly without file system I/O.
  - `FakeBlacklistRepository` fakes blacklist queries for testing SMS and Auto Call filtering.
  - `SmsSendingEngineTest` and `AutoCallEngineTest` validate atomic concurrent state changes, queue processing, timeout mechanics, and retry limits in pure unit tests.
  - `DatabaseMigrationTest` verifies table integrity and schema adjustments across versions v1 through v15.
- Isolation guarantees: Unit tests run in isolated JVM processes with no persistent disk or database side effects.
- Common failure mode in tests: Tests depending on unseeded pseudo-random number generators or platform-specific string encodings.

### 5) Coverage and Quality Signals

- Coverage tool + threshold: [TODO] Neither JaCoCo nor Kover is configured in `build.gradle.kts`; no coverage thresholds are enforced.
- Current reported coverage: [TODO] Not tracked in CI or build tasks.
- CI Automation: Continuous integration runs on GitHub Actions (`.github/workflows/ci.yml`), executing `./gradlew testDebugUnitTest` and `:app:validateUpdateJson` on every PR and push to `master` and `main`.
- Known gaps/flaky areas:
  - Live Android Accessibility node tree interaction in external `com.zing.zalo` app cannot be tested in headless JVM tests; relies on unit verification of `SendResult` state transitions.
  - `SmsForegroundService.kt` and `AutoCallForegroundService.kt` rely on Android framework IPC and telephony state.

### 6) Evidence

- `app/build.gradle.kts#L116-L123`
- `.github/workflows/ci.yml`
- `app/src/test/java/com/gmail/nthai/data/excel/ExcelParserTest.kt`
- `app/src/test/java/com/gmail/nthai/data/local/DatabaseMigrationTest.kt`
- `app/src/test/java/com/gmail/nthai/domain/engine/AutoCallEngineTest.kt`
- `app/src/test/java/com/gmail/nthai/domain/engine/SmsSendingEngineTest.kt`
- `app/src/test/java/com/gmail/nthai/domain/repository/FakeBlacklistRepository.kt`
- `app/src/test/java/com/gmail/nthai/domain/usecase/ProcessSimSendingUseCaseTest.kt`
- `app/src/test/java/com/gmail/nthai/util/PhoneNumberUtilsTest.kt`
- `app/src/androidTest/java/com/gmail/nthai/presentation/navigation/AppNavigationTest.kt`
