# Testing Patterns

## Core Sections (Required)

### 1) Test Stack and Commands

- Primary test framework: JUnit 4 (`junit:junit:4.13.2`) for JVM unit tests; AndroidX JUnit (`androidx.test.ext:junit:1.2.1`) for instrumented tests.
- Assertion/mocking tools: Standard `org.junit.Assert` (`assertEquals`, `assertTrue`, `assertNotNull`, `fail`), `kotlinx.coroutines.test` for testing coroutine dispatches. No external mocking library (e.g. Mockito, MockK) is integrated; tests rely on real in-memory data structures and deterministic fakes.
- Commands:

```bash
# Run all JVM unit tests
./gradlew test

# Run unit tests specifically for the app module
./gradlew :app:testDebugUnitTest

# Run instrumented UI and navigation tests on connected Android device/emulator
./gradlew connectedAndroidTest
```

### 2) Test Layout

- Test file placement pattern:
  - Unit tests: `app/src/test/java/com/gmail/nthai/`
  - Instrumented UI/Compose tests: `app/src/androidTest/java/com/gmail/nthai/`
- Naming convention: SourceClassName + `Test.kt` (e.g. `ParseSpintaxUseCaseTest.kt`, `ExcelParserTest.kt`, `ProcessSimSendingUseCaseTest.kt`, `AppNavigationTest.kt`).
- Setup files and where they run: Instrumented tests utilize `androidx.test.runner.AndroidJUnitRunner` declared as `testInstrumentationRunner` in `app/build.gradle.kts#L19`.

### 3) Test Scope Matrix

| Scope | Covered? | Typical target | Notes |
|-------|----------|----------------|-------|
| Unit | Yes | Domain UseCases (`ParseSpintaxUseCase`, `DetectVietnameseCarrierUseCase`, `CalculateNextDelayUseCase`, `ValidateSendingConstraintUseCase`, `ProcessSimSendingUseCase`), `SimConfigCarrierLimit`, and `ExcelParser` | Focused strictly on pure business logic, regex evaluation, and spreadsheet data transformation. `ExcelParserTest` covers custom header mappings, Vietnamese accents, and error scenarios. `ProcessSimSendingUseCaseTest` tests SMS dispatch, carrier quotas, and failure modes. |
| Integration | Partial | Room DAOs and database migrations | No standalone automated Room migration tests; relies on in-memory DB instances |
| E2E / UI | Partial | `AppNavigationTest.kt` in `androidTest` | Validates Compose navigation graph transitions via `androidx.compose.ui.test.junit4` |

### 4) Mocking and Isolation Strategy

- Main mocking approach: In-memory test fixtures and stateful test instances.
  - `ParseSpintaxUseCaseTest` creates customer instances with sample custom field maps (`Customer(...)`) and injects deterministic `kotlin.random.Random(seed)` instances to verify reproducible Spintax branch selection.
  - `ExcelParserTest` uses in-memory `ByteArrayOutputStream` with POI `XSSFWorkbook` to construct real binary Excel spreadsheets on the fly without file system I/O.
- Isolation guarantees: Unit tests run in isolated JVM processes with no persistent disk or database side effects.
- Common failure mode in tests: Tests depending on unseeded pseudo-random number generators or platform-specific string encodings.

### 5) Coverage and Quality Signals

- Coverage tool + threshold: [TODO] Neither JaCoCo nor Kover is configured in `build.gradle.kts`; no coverage thresholds are enforced.
- Current reported coverage: [TODO] Not tracked in CI or build tasks.
- Known gaps/flaky areas:
  - Zalo automation components (`ZaloAccessibilityService.kt`, `ZaloSendingEngine.kt`) have zero automated unit or UI test coverage due to Android Accessibility API and external app constraints.
  - `SmsForegroundService.kt` and `SmsStatusReceiver.kt` rely on Android framework IPC and lack headless unit tests.
  - Room migration paths (e.g. `1_2`, `4_5`, `11_12`) are not verified with `MigrationTestHelper`.

### 6) Evidence

- `app/build.gradle.kts#L95-L101`
- `app/src/test/java/com/gmail/nthai/data/excel/ExcelParserTest.kt`
- `app/src/test/java/com/gmail/nthai/domain/model/SimConfigCarrierLimitTest.kt`
- `app/src/test/java/com/gmail/nthai/domain/usecase/CalculateNextDelayUseCaseTest.kt`
- `app/src/test/java/com/gmail/nthai/domain/usecase/DetectVietnameseCarrierUseCaseTest.kt`
- `app/src/test/java/com/gmail/nthai/domain/usecase/ParseSpintaxUseCaseTest.kt`
- `app/src/test/java/com/gmail/nthai/domain/usecase/ProcessSimSendingUseCaseTest.kt`
- `app/src/test/java/com/gmail/nthai/domain/usecase/ValidateSendingConstraintUseCaseTest.kt`
- `app/src/androidTest/java/com/gmail/nthai/presentation/navigation/AppNavigationTest.kt`
