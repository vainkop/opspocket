# OpsPocket - Android App

## Project Overview

Native Android app built with Kotlin + Jetpack Compose.

- **Package**: `com.vainkop.opspocket`
- **Min SDK**: 26 (Android 8.0)
- **Target/Compile SDK**: 35 (Android 15)
- **Build system**: Gradle 8.11.1 with Kotlin DSL
- **UI**: Jetpack Compose with Material 3 + dynamic color

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
│       │   │   ├── MainActivity.kt           # Entry point
│       │   │   └── ui/theme/Theme.kt         # Material 3 theme
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
// In build.gradle.kts
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.material3)
```

### Pre-configured libraries (uncomment in build.gradle.kts when needed)

Available in the version catalog but not all wired into dependencies yet:

| Category | Libraries |
|---|---|
| **Networking** | OkHttp, Retrofit, kotlinx-serialization |
| **DI** | Hilt + Hilt Navigation Compose |
| **Database** | Room (runtime, ktx, compiler) |
| **Storage** | DataStore Preferences |
| **Images** | Coil Compose |
| **Navigation** | Navigation Compose (already wired) |
| **Coroutines** | kotlinx-coroutines (already wired) |

To add e.g. Hilt: add the plugin to `build.gradle.kts` (root + app) and add the dependency.

## Conventions

- Language: **Kotlin** (no Java)
- UI: **Jetpack Compose** (no XML layouts)
- Build DSL: **Kotlin DSL** (`.gradle.kts`)
- Architecture: follow standard Android architecture (ViewModel + Repository + data layer)
- Always use `./gradlew` (wrapper), not the system `gradle`
- Debug builds use applicationId suffix `.debug` so both debug and release can coexist on a device

## Do Not Commit

- `local.properties` — machine-specific SDK path
- `*.keystore` / `*.jks` — signing keys
- `build/` directories
- `.gradle/` — Gradle cache
- API keys, secrets, credentials
