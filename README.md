# OpsPocket

Cloud infrastructure management from your phone. Manage Cast.ai Kubernetes clusters and Azure Virtual Machines on Android and iOS.

Built with **Kotlin Multiplatform** + **Compose Multiplatform** — one shared codebase, native apps on both platforms.

## Features

### Cast.ai
- **Cluster Management** - View all clusters dynamically fetched from Cast.ai API with status, region, and provider info
- **One-Tap Rebalancing** - Trigger cluster rebalancing directly from your phone (create plan + execute)
- **Secure API Key Storage** - Keys encrypted on device (EncryptedSharedPreferences on Android, Keychain on iOS); rotate or delete at any time
- **Status Indicators** - Color-coded chips for cluster status (ready/warning/failed/hibernated) and agent connectivity (online/disconnected)

### Azure
- **Sign in with Microsoft** - Device Code Flow authentication, no app registration required
- **Multi-Tenant Support** - Switch between Azure AD tenants
- **Smart Subscription Sorting** - Subscriptions sorted by last-used first, then by usage frequency
- **VM Management** - List all virtual machines with power state indicators (Running/Stopped/Deallocated)
- **Power Operations** - Start, Stop, Deallocate, and Restart VMs with confirmation dialogs

### General
- **Cross-Platform** - Android and iOS from a single Kotlin codebase
- **Pull-to-Refresh** - Swipe down to refresh any list
- **Dark Mode** - Full dark theme support

## Requirements

- **Android**: Android 14 (API 34) or higher
- **iOS**: Requires Mac with Xcode to build (project skeleton included)
- Cast.ai account with API key (for Cast.ai features)
- Microsoft account with Azure subscription (for Azure features)

## Getting Started

### Prerequisites

- JDK 17
- Android SDK (API 35)
- Android Studio or command-line tools

### Build

```bash
git clone https://github.com/vainkop/opspocket.git
cd opspocket
./gradlew :composeApp:assembleDebug
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

### Run Tests

```bash
./gradlew :composeApp:testDebugUnitTest    # Shared + Android unit tests
```

### Download APK

Pre-built APKs are available on the [Releases](https://github.com/vainkop/opspocket/releases) page.

## Usage

### Cast.ai

1. Open OpsPocket
2. Tap **Manage Cast.ai**
3. Paste your `CASTAI_API_KEY` and tap **Connect**
4. Browse your clusters
5. Tap a cluster to view details
6. Tap **Trigger Rebalancing** to optimize node configuration

#### API Key Management

- Tap the key icon in the cluster list toolbar to change or delete your API key
- Keys can be rotated at any time - enter a new one and it replaces the old
- Deleting a key returns you to the home screen

#### Rebalancing

The rebalancing flow matches the [Cast.ai rebalancing API](https://docs.cast.ai/docs/rebalancing):

1. Checks cluster status (must be "ready")
2. Creates a rebalancing plan with `minNodes: 1`
3. Waits 5 seconds for plan generation
4. Executes the plan

Clusters in hibernated, hibernating, failed, or other non-ready states are blocked from rebalancing with a dialog explaining why.

### Azure

1. Open OpsPocket
2. Tap **Manage Azure**
3. Tap **Sign in with Microsoft**
4. Copy the device code and tap **Copy code & open browser**
5. Enter the code at microsoft.com/devicelogin and authenticate
6. Select your tenant (auto-selected if only one)
7. Select your subscription (auto-selected if only one)
8. Browse your virtual machines
9. Tap a VM to view details and perform power operations

#### Power Operations

- **Start** - Boot a stopped/deallocated VM
- **Stop** - Shut down the OS (VM stays allocated, billing continues)
- **Deallocate** - Shut down and release compute resources (billing stops, dynamic IPs released)
- **Restart** - Reboot a running VM

All power operations require confirmation before executing.

#### Settings

Tap the gear icon in the VM list toolbar to change tenant or subscription.

## Architecture

Clean Architecture with strict layer separation, fully shared across platforms:

```
presentation/  ->  domain/  ->  data/
(UI + ViewModel)   (UseCases)   (API + Storage)
```

- **Presentation**: Compose Multiplatform screens, Koin ViewModels, sealed UI states, MVI pattern
- **Domain**: Use cases, domain models, repository interfaces
- **Data**: Ktor API clients, kotlinx-serialization DTOs, expect/actual SecureStorage, multiplatform-settings

### Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin 2.1 (Multiplatform) |
| UI | Compose Multiplatform 1.8.2 + Material 3 |
| DI | Koin 4.1.1 |
| Networking | Ktor 3.1.1 |
| Serialization | kotlinx-serialization |
| Navigation | JetBrains Navigation Compose 2.9.0 |
| Lifecycle | JetBrains Lifecycle 2.9.0 |
| State | StateFlow + MVI |
| Security | EncryptedSharedPreferences (Android) / Keychain (iOS) |
| Preferences | multiplatform-settings 1.3.0 |
| Auth | OAuth2 Device Code Flow |
| Build | Gradle 8.11.1 (Kotlin DSL) |
| CI | GitHub Actions |
| Android | Min SDK 34 (Android 14), Target SDK 35 |
| iOS | Xcode project skeleton (requires Mac to build) |

### Project Structure

```
opspocket/
├── composeApp/                  # KMP + Compose Multiplatform module
│   └── src/
│       ├── commonMain/          # Shared code (72 files) - domain, data, DI, UI, navigation
│       ├── commonTest/          # Shared tests (12 files)
│       ├── androidMain/         # Android-specific (5 files) - entry point, secure storage, resources
│       └── iosMain/             # iOS-specific (4 files) - entry point, secure storage
├── iosApp/                      # Xcode project skeleton (Swift entry point)
└── gradle/libs.versions.toml    # Centralized dependency versions
```

## CI/CD

- **CI** (`build.yml`): Runs unit tests and builds the APK on every push/PR to `main`
- **Release** (`release.yml`): Creates a GitHub Release with the APK when a version tag is pushed (`v*`), or on manual trigger via `workflow_dispatch`

To create a release:
```bash
git tag v0.3.0
git push origin v0.3.0
```

iOS builds require a Mac runner (`macos-latest`) — not yet configured in CI.

## Roadmap

- [ ] iOS build pipeline (macOS CI runner + TestFlight)
- [ ] iOS Keychain integration (replace NSUserDefaults placeholder)
- [ ] Azure resource group filtering
- [ ] VM auto-refresh on power state change
- [ ] Cluster node list view
- [ ] Rebalancing history
- [ ] Push notifications for operation status
- [ ] Biometric authentication
- [ ] Multiple API key profiles

## License

Proprietary - All rights reserved.
