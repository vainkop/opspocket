# OpsPocket - Kotlin Multiplatform App

## Project Overview

Cross-platform mobile app for managing cloud infrastructure (Cast.ai + Azure) from Android and iOS.
Built with Kotlin Multiplatform (KMP) + Compose Multiplatform (CMP), targeting Android 14+ and iOS.

- **Package**: `com.vainkop.opspocket`
- **Min SDK (Android)**: 34 (Android 14)
- **Target/Compile SDK**: 35 (Android 15)
- **Build system**: Gradle 8.11.1 with Kotlin DSL
- **UI**: Compose Multiplatform with Material 3
- **Architecture**: Clean Architecture (data / domain / presentation)
- **DI**: Koin 4.1.1
- **Networking**: Ktor 3.1.1 + kotlinx-serialization
- **Security**: expect/actual SecureStorage (EncryptedSharedPreferences on Android, NSUserDefaults on iOS)
- **Preferences**: multiplatform-settings 1.3.0
- **Auth**: Azure Device Code Flow (OAuth2, no app registration required)

## Development Environment

| Tool | Version | Path |
|---|---|---|
| JDK | OpenJDK 17.0.18 | `/usr/lib/jvm/java-17-openjdk-amd64` |
| Android SDK | API 35 | `~/Android/Sdk` |
| Build Tools | 35.0.0 | `~/Android/Sdk/build-tools/35.0.0` |
| NDK | r27c (27.2.12479018) | `~/Android/Sdk/ndk/27.2.12479018` |
| Platform Tools | 36.0.2 | `~/Android/Sdk/platform-tools` |
| Emulator | 36.4.9 | `~/Android/Sdk/emulator` |
| Gradle | 8.11.1 (via wrapper) | `./gradlew` |

Environment variables are in `~/.bashrc`: `JAVA_HOME`, `ANDROID_HOME`, `ANDROID_SDK_ROOT`, `ANDROID_NDK_HOME`.

iOS builds require a Mac with Xcode (not available on Linux).

## Build & Test

```bash
./gradlew :composeApp:assembleDebug          # Build debug APK -> composeApp/build/outputs/apk/debug/
./gradlew :composeApp:assembleRelease        # Build release APK (needs signing config)
./gradlew :composeApp:testDebugUnitTest      # Unit tests (shared + Android)
./gradlew :composeApp:lint                   # Android lint
./gradlew clean                              # Clean build outputs
```

## Install & Run

```bash
adb devices                      # List connected devices
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk
adb shell am start -n com.vainkop.opspocket.debug/com.vainkop.opspocket.MainActivity
adb logcat -s OpsPocket          # Filter logs by tag
```

## SDK Management

```bash
sdkmanager --list_installed      # Show installed packages
sdkmanager --update              # Update all packages
sdkmanager "platforms;android-XX" # Install specific API level
avdmanager create avd -n Pixel_7 -k "system-images;android-35;google_apis;x86_64"
emulator -avd Pixel_7            # Launch emulator
```

## Architecture

Shared code lives in `commonMain`. Platform-specific code in `androidMain` and `iosMain`.

```
composeApp/src/commonMain/kotlin/com/vainkop/opspocket/
├── App.kt                        # Root @Composable entry point
├── data/                         # Data layer
│   ├── local/                    # SecureStorage (expect), SecureApiKeyStorage, AzureAuthManager, AzurePreferences
│   ├── mapper/                   # DTO -> Domain mappers (ClusterMapper, AzureMapper)
│   ├── remote/                   # Ktor API clients + DTOs
│   │   ├── dto/                  # Cast.ai + Azure serializable DTOs
│   │   ├── CastAiApiClient.kt   # Cast.ai Ktor client
│   │   ├── AzureManagementApiClient.kt  # Azure Management Ktor client
│   │   ├── AzureAuthApiClient.kt        # Azure Auth Ktor client (Device Code Flow)
│   │   └── HttpClientFactory.kt         # Creates configured Ktor HttpClient instances
│   └── repository/               # Repository implementations (CastAiRepositoryImpl, AzureRepositoryImpl)
├── di/                           # Koin modules (AppModules.kt: storage, network, repository, useCase, viewModel)
├── domain/                       # Domain layer
│   ├── model/                    # Domain models + AppResult sealed class
│   ├── repository/               # Repository interfaces (CastAiRepository, AzureRepository)
│   └── usecase/                  # Use cases (ValidateApiKey, GetClusters, GetTenants, GetVMs, PerformVmAction, etc.)
├── navigation/                   # Compose Navigation (Screen routes, AppNavigation NavHost)
├── presentation/                 # Presentation layer
│   ├── apikey/                   # API key entry/management screen + ViewModel
│   ├── azureauth/                # Azure Device Code Flow sign-in screen + ViewModel
│   ├── azuresetup/               # Azure tenant/subscription selection screen + ViewModel
│   ├── clusterdetails/           # Cluster details + rebalancing screen + ViewModel
│   ├── clusterlist/              # Cluster list screen + ViewModel
│   ├── common/                   # Shared UI components (StatusChip, VmPowerStateChip, LoadingIndicator, etc.)
│   ├── home/                     # Home screen
│   ├── vmdetails/                # VM details + power operations screen + ViewModel
│   └── vmlist/                   # VM list screen + ViewModel
└── ui/theme/                     # Material 3 theme (dark/light)
```

### Platform-specific code

```
composeApp/src/androidMain/kotlin/com/vainkop/opspocket/
├── MainActivity.kt               # Android entry point (ComponentActivity)
├── OpsPocketApplication.kt       # Starts Koin with Android context
├── data/local/
│   ├── SecureStorage.android.kt  # actual: EncryptedSharedPreferences
│   └── CurrentTimeMillis.android.kt
└── di/
    └── PlatformModule.android.kt # Android Koin module (context, SharedPreferencesSettings)

composeApp/src/iosMain/kotlin/com/vainkop/opspocket/
├── MainViewController.kt         # iOS entry point (ComposeUIViewController)
├── data/local/
│   ├── SecureStorage.ios.kt      # actual: NSUserDefaults (placeholder for Keychain)
│   └── CurrentTimeMillis.ios.kt
└── di/
    └── PlatformModule.ios.kt     # iOS Koin module (NSUserDefaultsSettings)
```

## Project Structure

```
opspocket/
├── composeApp/
│   ├── build.gradle.kts              # KMP + CMP module build config
│   ├── proguard-rules.pro            # R8/ProGuard rules for release
│   └── src/
│       ├── commonMain/               # Shared Kotlin code (72 files)
│       ├── commonTest/               # Shared unit tests (12 files)
│       ├── androidMain/              # Android-specific code (5 files) + resources
│       │   ├── AndroidManifest.xml
│       │   ├── kotlin/...
│       │   └── res/
│       └── iosMain/                  # iOS-specific code (4 files)
├── iosApp/
│   └── iosApp/
│       ├── iOSApp.swift              # SwiftUI entry point
│       ├── ContentView.swift         # Wraps ComposeUIViewController
│       └── Info.plist                # iOS app metadata
├── build.gradle.kts                  # Root build file (plugin declarations)
├── settings.gradle.kts               # Module includes (composeApp)
├── gradle/
│   ├── libs.versions.toml            # Version catalog (all dependency versions)
│   └── wrapper/                      # Gradle wrapper (committed)
├── gradle.properties                 # Build performance flags + KMP settings
├── local.properties                  # SDK path (DO NOT commit)
├── .editorconfig                     # Code style
└── .gitignore
```

## Dependency Management

All versions are centralized in `gradle/libs.versions.toml`. Dependencies are declared per source set in `composeApp/build.gradle.kts`:

```kotlin
// commonMain
implementation(compose.material3)
implementation(libs.ktor.client.core)
implementation(libs.koin.core)

// androidMain
implementation(libs.ktor.client.android)
implementation(libs.koin.android)

// iosMain
implementation(libs.ktor.client.darwin)
```

### Active Dependencies

| Category | Libraries |
|---|---|
| **UI** | Compose Multiplatform 1.8.2 (Material 3, Icons Extended, Foundation) |
| **Lifecycle** | JetBrains Lifecycle 2.9.0 (viewmodel-compose, runtime-compose) |
| **Navigation** | JetBrains Navigation Compose 2.9.0-beta03 |
| **Networking** | Ktor 3.1.1 (core, content-negotiation, serialization, logging, android/darwin engines) |
| **Serialization** | kotlinx-serialization-json 1.7.3 |
| **DI** | Koin 4.1.1 (core, compose, compose-viewmodel, android) |
| **Security** | AndroidX Security Crypto 1.0.0 (Android only, for EncryptedSharedPreferences) |
| **Preferences** | multiplatform-settings 1.3.0 (replaces DataStore) |
| **Coroutines** | kotlinx-coroutines 1.9.0 (core + android) |
| **Android** | AndroidX Core KTX 1.15.0, Activity Compose 1.9.3 |
| **Testing** | kotlin-test, kotlinx-coroutines-test |

## Cast.ai API Integration

The app communicates with Cast.ai via Ktor `CastAiApiClient`:

| Endpoint | Method | Purpose |
|---|---|---|
| `/v1/kubernetes/external-clusters` | GET | List all clusters (also validates API key) |
| `/v1/kubernetes/external-clusters/{id}` | GET | Get cluster details |
| `/v1/kubernetes/clusters/{id}/rebalancing-plans` | POST | Create rebalancing plan (body: `{"minNodes": 1}`) |
| `/v1/kubernetes/clusters/{id}/rebalancing-plans/{planId}/execute` | POST | Execute rebalancing plan |

Authentication: `X-API-Key` header injected via Ktor `defaultRequest` block in `HttpClientFactory`.

### Rebalancing Flow

1. Check cluster status (must be "ready")
2. POST create plan -> get `rebalancingPlanId`
3. Wait 5 seconds for plan generation
4. POST execute plan
5. Show result

## Azure API Integration

Authentication uses the **Device Code Flow** (OAuth2) with Azure PowerShell's well-known public client ID (`1950a258-227b-4e31-a9cf-717495945fc2`). No Azure AD app registration is required from anyone.

### Auth Flow

1. POST `/common/oauth2/v2.0/devicecode` -> get user code + verification URL
2. User opens `https://microsoft.com/devicelogin` in browser, enters code, authenticates
3. App polls `/common/oauth2/v2.0/token` until authentication completes
4. Access + refresh tokens stored encrypted via SecureStorage (EncryptedSharedPreferences on Android)
5. Token refresh via `/tenant/oauth2/v2.0/token` for tenant switching

### Azure Management API Endpoints

Base URL: `https://management.azure.com/`

| Endpoint | Method | Purpose |
|---|---|---|
| `/tenants` | GET | List Azure AD tenants |
| `/subscriptions` | GET | List subscriptions |
| `/subscriptions/{sub}/providers/Microsoft.Compute/virtualMachines` | GET | List VMs (power state fetched per-VM) |
| `/subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.Compute/virtualMachines/{vm}?$expand=instanceView` | GET | VM with power state |
| `.../virtualMachines/{vm}/start` | POST | Start VM |
| `.../virtualMachines/{vm}/powerOff` | POST | Stop VM |
| `.../virtualMachines/{vm}/deallocate` | POST | Deallocate VM |
| `.../virtualMachines/{vm}/restart` | POST | Restart VM |

Authentication: `Authorization: Bearer {token}` header injected via Ktor `defaultRequest` block in `HttpClientFactory`.

### Navigation Flow

```
Home -> AzureAuth (Device Code sign-in) -> AzureSetup (tenant -> subscription) -> VmList -> VmDetails (power ops)
```

Tenant + subscription selection persisted via multiplatform-settings across app restarts.
Subscriptions sorted by last-used first, then by usage frequency.

## CI/CD

Two GitHub Actions workflows in `.github/workflows/`:

- **`build.yml` (CI)**: Runs on every push/PR to `main`. Runs unit tests and builds the debug APK. No release created.
- **`release.yml` (Release)**: Runs on version tags (`v*`) or manual `workflow_dispatch`. Builds APK, uploads artifact, creates GitHub Release with APK attached.

To create a release:
```bash
git tag v0.3.0
git push origin v0.3.0
```

iOS builds require a Mac runner (`macos-latest`) — not yet configured in CI.

## Conventions

- Language: **Kotlin** (no Java)
- UI: **Compose Multiplatform** (no XML layouts, no platform-specific UI)
- Build DSL: **Kotlin DSL** (`.gradle.kts`)
- Architecture: Clean Architecture (ViewModel + UseCase + Repository)
- Pattern: MVI / unidirectional data flow with sealed UI states
- State: StateFlow in ViewModels, collectAsStateWithLifecycle in Compose
- DI: Koin modules in `di/AppModules.kt`, platform modules in `androidMain`/`iosMain`
- ViewModels use plain constructors (no annotations), parameters passed via Koin `parametersOf`
- Screens use `koinViewModel()` or `koinViewModel { parametersOf(...) }` instead of `hiltViewModel()`
- Platform abstractions use `expect`/`actual` (e.g., `SecureStorage`, `currentTimeMillis`)
- Always use `./gradlew` (wrapper), not the system `gradle`
- Debug builds use applicationId suffix `.debug` so both debug and release can coexist
- Each Ktor API client gets its own dedicated HttpClient instance via `HttpClientFactory` (no shared/ambiguous instances)

## Do Not Commit

- `local.properties` - machine-specific SDK path
- `*.keystore` / `*.jks` - signing keys
- `build/` directories
- `.gradle/` - Gradle cache
- API keys, secrets, credentials
