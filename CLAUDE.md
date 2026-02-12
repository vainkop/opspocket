# OpsPocket - Android App

## Project Overview

Native Android app for managing cloud infrastructure (Cast.ai + Azure) from mobile.
Built with Kotlin + Jetpack Compose, targeting modern Android 14+.

- **Package**: `com.vainkop.opspocket`
- **Min SDK**: 34 (Android 14)
- **Target/Compile SDK**: 35 (Android 15)
- **Build system**: Gradle 8.11.1 with Kotlin DSL
- **UI**: Jetpack Compose with Material 3 + dynamic color
- **Architecture**: Clean Architecture (data / domain / presentation)
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp + kotlinx-serialization
- **Security**: EncryptedSharedPreferences for API key & token storage
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

## Build & Test

```bash
./gradlew assembleDebug          # Build debug APK -> app/build/outputs/apk/debug/
./gradlew assembleRelease        # Build release APK (needs signing config)
./gradlew test                   # Unit tests (JVM)
./gradlew connectedAndroidTest   # Instrumented tests (device/emulator)
./gradlew lint                   # Android lint
./gradlew clean                  # Clean build outputs
```

## Install & Run

```bash
adb devices                      # List connected devices
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.vainkop.opspocket.debug/.MainActivity
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

```
com.vainkop.opspocket/
├── data/                         # Data layer
│   ├── local/                    # SecureApiKeyStorage, AzureTokenStorage, AzureAuthManager, AzurePreferences
│   ├── mapper/                   # DTO -> Domain mappers (ClusterMapper, AzureMapper)
│   ├── remote/                   # Retrofit APIs + DTOs + Interceptors
│   │   ├── dto/                  # Cast.ai + Azure serializable DTOs
│   │   └── interceptor/          # AuthInterceptor (Cast.ai), AzureAuthInterceptor (Bearer token)
│   └── repository/               # Repository implementations
├── di/                           # Hilt modules (NetworkModule, AzureNetworkModule, RepositoryModule, AzureRepositoryModule)
├── domain/                       # Domain layer
│   ├── model/                    # Domain models + AppResult sealed class
│   ├── repository/               # Repository interfaces (CastAiRepository, AzureRepository)
│   └── usecase/                  # Use cases (ValidateApiKey, GetClusters, GetTenants, GetVMs, PerformVmAction, etc.)
├── navigation/                   # Compose Navigation (Screen routes, NavHost)
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
└── ui/theme/                     # Material 3 theme
```

## Project Structure

```
opspocket/
├── app/
│   ├── build.gradle.kts              # App module build config
│   ├── proguard-rules.pro            # R8/ProGuard rules for release
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── kotlin/com/vainkop/opspocket/
│       │   │   ├── MainActivity.kt           # Entry point (@AndroidEntryPoint)
│       │   │   ├── OpsPocketApplication.kt   # Hilt Application
│       │   │   ├── data/                     # Data layer
│       │   │   ├── di/                       # DI modules
│       │   │   ├── domain/                   # Domain layer
│       │   │   ├── navigation/               # Navigation
│       │   │   ├── presentation/             # Screens + ViewModels
│       │   │   └── ui/theme/                 # Theme
│       │   └── res/                          # Resources
│       ├── test/                             # Unit tests (JVM)
│       └── androidTest/                      # Instrumented tests
├── build.gradle.kts                  # Root build file (plugin declarations)
├── settings.gradle.kts               # Module includes, repositories
├── gradle/
│   ├── libs.versions.toml            # Version catalog (all dependency versions)
│   └── wrapper/                      # Gradle wrapper (committed)
├── gradle.properties                 # Build performance flags
├── local.properties                  # SDK path (DO NOT commit)
├── .editorconfig                     # Code style
└── .gitignore
```

## Dependency Management

All versions are centralized in `gradle/libs.versions.toml`. Use version catalog references:

```kotlin
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.material3)
```

### Active Dependencies

| Category | Libraries |
|---|---|
| **Core** | AndroidX Core KTX, Lifecycle (runtime, viewmodel, compose) |
| **UI** | Jetpack Compose (BOM), Material 3, Material Icons Extended |
| **Navigation** | Navigation Compose |
| **Networking** | OkHttp + logging, Retrofit + kotlinx-serialization converter |
| **Serialization** | kotlinx-serialization-json |
| **DI** | Hilt + Hilt Navigation Compose + KSP compiler |
| **Security** | AndroidX Security Crypto (EncryptedSharedPreferences) |
| **Storage** | DataStore Preferences (Azure tenant/subscription selection) |
| **Coroutines** | kotlinx-coroutines-android |
| **Testing** | JUnit 4, Espresso, Compose UI testing, Coroutines test, Hilt testing |

### Available but not wired

| Category | Libraries |
|---|---|
| **Database** | Room (runtime, ktx, compiler) |
| **Images** | Coil Compose |

## Cast.ai API Integration

The app communicates with Cast.ai via these endpoints:

| Endpoint | Method | Purpose |
|---|---|---|
| `/v1/kubernetes/external-clusters` | GET | List all clusters (also validates API key) |
| `/v1/kubernetes/external-clusters/{id}` | GET | Get cluster details |
| `/v1/kubernetes/clusters/{id}/rebalancing-plans` | POST | Create rebalancing plan (body: `{"minNodes": 1}`) |
| `/v1/kubernetes/clusters/{id}/rebalancing-plans/{planId}/execute` | POST | Execute rebalancing plan |

Authentication: `X-API-Key` header injected via OkHttp interceptor.

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
4. Access + refresh tokens stored encrypted via EncryptedSharedPreferences
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

Authentication: `Authorization: Bearer {token}` header injected via OkHttp interceptor.

### Navigation Flow

```
Home -> AzureAuth (Device Code sign-in) -> AzureSetup (tenant -> subscription) -> VmList -> VmDetails (power ops)
```

Tenant + subscription selection persisted via DataStore Preferences across app restarts.

## CI/CD

Two GitHub Actions workflows in `.github/workflows/`:

- **`build.yml` (CI)**: Runs on every push/PR to `main`. Runs unit tests and builds the debug APK. No release created.
- **`release.yml` (Release)**: Runs on version tags (`v*`) or manual `workflow_dispatch`. Builds APK, uploads artifact, creates GitHub Release with APK attached.

To create a release:
```bash
git tag v0.2.0
git push origin v0.2.0
```

## Conventions

- Language: **Kotlin** (no Java)
- UI: **Jetpack Compose** (no XML layouts)
- Build DSL: **Kotlin DSL** (`.gradle.kts`)
- Architecture: Clean Architecture (ViewModel + UseCase + Repository)
- Pattern: MVI / unidirectional data flow with sealed UI states
- State: StateFlow in ViewModels, collectAsStateWithLifecycle in Compose
- Always use `./gradlew` (wrapper), not the system `gradle`
- Debug builds use applicationId suffix `.debug` so both debug and release can coexist
- Azure networking uses `@Named("azure")` qualifiers to avoid DI conflicts with Cast.ai

## Do Not Commit

- `local.properties` - machine-specific SDK path
- `*.keystore` / `*.jks` - signing keys
- `build/` directories
- `.gradle/` - Gradle cache
- API keys, secrets, credentials
