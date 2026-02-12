# OpsPocket - Android App

## Project Overview

Native Android app for managing cloud infrastructure (Cast.ai) from mobile.
Built with Kotlin + Jetpack Compose, targeting modern Android 14+.

- **Package**: `com.vainkop.opspocket`
- **Min SDK**: 34 (Android 14)
- **Target/Compile SDK**: 35 (Android 15)
- **Build system**: Gradle 8.11.1 with Kotlin DSL
- **UI**: Jetpack Compose with Material 3 + dynamic color
- **Architecture**: Clean Architecture (data / domain / presentation)
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp + kotlinx-serialization
- **Security**: EncryptedSharedPreferences for API key storage

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
│   ├── local/                    # SecureApiKeyStorage (EncryptedSharedPreferences)
│   ├── mapper/                   # DTO -> Domain mappers
│   ├── remote/                   # Retrofit API + DTOs + AuthInterceptor
│   └── repository/               # Repository implementations
├── di/                           # Hilt modules (NetworkModule, RepositoryModule)
├── domain/                       # Domain layer
│   ├── model/                    # Domain models + AppResult sealed class
│   ├── repository/               # Repository interfaces
│   └── usecase/                  # Use cases (ValidateApiKey, GetClusters, etc.)
├── navigation/                   # Compose Navigation (Screen routes, NavHost)
├── presentation/                 # Presentation layer
│   ├── apikey/                   # API key entry/management screen + ViewModel
│   ├── clusterdetails/           # Cluster details + rebalancing screen + ViewModel
│   ├── clusterlist/              # Cluster list screen + ViewModel
│   ├── common/                   # Shared UI components (StatusChip, LoadingIndicator, etc.)
│   └── home/                     # Home screen
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
| **Coroutines** | kotlinx-coroutines-android |
| **Testing** | JUnit 4, Espresso, Compose UI testing, Coroutines test, Hilt testing |

### Available but not wired

| Category | Libraries |
|---|---|
| **Database** | Room (runtime, ktx, compiler) |
| **Storage** | DataStore Preferences |
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

## CI/CD

Two GitHub Actions workflows in `.github/workflows/`:

- **`build.yml` (CI)**: Runs on every push/PR to `main`. Runs unit tests and builds the debug APK. No release created.
- **`release.yml` (Release)**: Runs on version tags (`v*`) or manual `workflow_dispatch`. Builds APK, uploads artifact, creates GitHub Release with APK attached.

To create a release:
```bash
git tag v0.1.0
git push origin v0.1.0
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

## Do Not Commit

- `local.properties` - machine-specific SDK path
- `*.keystore` / `*.jks` - signing keys
- `build/` directories
- `.gradle/` - Gradle cache
- API keys, secrets, credentials
