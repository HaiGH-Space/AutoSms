# Coding Conventions

## Core Sections (Required)

### 1) Naming Rules

| Item | Rule | Example | Evidence |
|------|------|---------|----------|
| Files | PascalCase for Kotlin source files and composable screens | `HomeScreen.kt`, `ExcelParser.kt`, `SmsForegroundService.kt` | `app/src/main/java/com/gmail/nthai/` |
| Functions/methods | camelCase with descriptive verbs | `calculateNextDelay()`, `detectCarrier()`, `parseSpintax()` | `domain/usecase/CalculateNextDelayUseCase.kt` |
| Composable Functions | PascalCase named as UI nouns or screens | `HomeScreen`, `CustomerListScreen`, `SimManagementTab` | `presentation/screens/HomeScreen.kt` |
| Types/interfaces | PascalCase with suffix denoting role (`Dao`, `Entity`, `UseCase`, `Repository`) | `CustomerDao`, `CustomerEntity`, `ParseSpintaxUseCase`, `CustomerRepository` | `domain/repository/CustomerRepository.kt` |
| Constants | UPPER_SNAKE_CASE | `ACTION_START`, `CHANNEL_ID`, `NOTIFICATION_ID`, `DEFAULT_UPDATE_URL` | `service/SmsForegroundService.kt#L389-L396` |
| XML Resources | snake_case prefixed by resource category | `activity_main.xml`, `zalo_accessibility_config.xml`, `ic_launcher.xml` | `app/src/main/res/` |

### 2) Formatting and Linting

- Formatter: Official Kotlin code style configured via Gradle project properties (`kotlin.code.style=official`).
- Linter: Built-in Android Studio / AGP lint checks; no external `.eslintrc`, `detekt.yml`, or `ktlint` configuration files exist in the repository root.
- Most relevant enforced rules:
  1. Kotlin official indentation (4 spaces, no tabs).
  2. CamelCase method names, PascalCase class and composable names.
  3. No wildcard imports (every imported type is explicitly referenced).
- Run commands:
  ```bash
  # Check Android lint
  ./gradlew lint
  ```

### 3) Import and Module Conventions

- Import grouping/order: Standard Android Studio grouping: Android platform / Jetpack imports first, followed by third-party libraries (Hilt, Room, Apache POI), Kotlin standard library/coroutines, and application internal packages (`com.gmail.nthai.*`).
- Alias vs relative import policy: Full absolute package references used; Kotlin import aliases are used sparingly only for disambiguating duplicate names.
- Public exports/barrel policy: No barrel files (not applicable to Kotlin/JVM). Public APIs are defined via domain repository interfaces and models; implementation classes (such as `*Impl`) reside in `data/`.

### 4) Error and Logging Conventions

- Error strategy by layer:
  - `data/`: Try-catch blocks catching `Exception` with fallback values or logging errors via `android.util.Log.e(...)`; Room operations utilize coroutines and SQLite transactions.
  - `domain/`: Pure calculations validate parameters and return safe defaults (e.g. empty string, fallback duration, or boolean status) without unhandled exceptions.
  - `presentation/`: ViewModels catch exceptions and update UI state data classes with human-readable error messages for snackbars or dialogs.
- Logging style and required context fields: Native Android logging (`android.util.Log.d(TAG, ...)`, `Log.e(TAG, ...)`, `Log.w(TAG, ...)`) using a class-level `TAG` constant (e.g. `"SmsForegroundService"`, `"ZaloAccessibilityService"`).
- Sensitive-data redaction rules: Customer phone numbers and message previews are logged locally to Logcat during debug runs; no remote telemetry or cloud logging service is integrated.

### 5) Testing Conventions

- Test file naming/location rule: Named with the suffix `Test.kt` (e.g. `ParseSpintaxUseCaseTest.kt`, `ExcelParserTest.kt`), mirroring the package structure under `app/src/test/java/com/gmail/nthai/` for unit tests and `app/src/androidTest/java/com/gmail/nthai/` for instrumented tests.
- Mocking strategy norm: Pure Kotlin testing using real in-memory models, deterministic random seeds (`Random(42)`), and lightweight fakes. No third-party mocking framework (such as MockK or Mockito) is currently declared in `build.gradle.kts`.
- Coverage expectation: [TODO] No code coverage tool (JaCoCo/Kover) or automated coverage gate is currently enforced in the build configuration.

### 6) Evidence

- `gradle.properties`
- `app/src/main/java/com/gmail/nthai/service/SmsForegroundService.kt`
- `app/src/main/java/com/gmail/nthai/domain/usecase/ParseSpintaxUseCase.kt`
- `app/src/test/java/com/gmail/nthai/domain/usecase/ParseSpintaxUseCaseTest.kt`

