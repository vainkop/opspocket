# OpsPocket

Cloud infrastructure management from your Android device. Manage Cast.ai Kubernetes clusters and trigger rebalancing on the go.

## Features

- **Cast.ai Cluster Management** - View all clusters dynamically fetched from Cast.ai API with status, region, and provider info
- **One-Tap Rebalancing** - Trigger cluster rebalancing directly from your phone (create plan + execute)
- **Secure API Key Storage** - Keys encrypted on device via EncryptedSharedPreferences; rotate or delete at any time
- **Status Indicators** - Color-coded chips for cluster status (ready/warning/failed/hibernated) and agent connectivity (online/disconnected)
- **Pull-to-Refresh** - Swipe down to refresh cluster list
- **Material You** - Dynamic color theming on Android 12+
- **Dark Mode** - Full dark theme support
- **Azure (Coming Soon)** - Placeholder for future Azure integration

## Requirements

- Android 14 (API 34) or higher
- Cast.ai account with API key

## Getting Started

### Prerequisites

- JDK 17
- Android SDK (API 35)
- Android Studio or command-line tools

### Build

```bash
git clone https://github.com/vainkop/opspocket.git
cd opspocket
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Run Tests

```bash
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests (requires device/emulator)
```

### Download APK

Pre-built APKs are available on the [Releases](https://github.com/vainkop/opspocket/releases) page.

## Usage

1. Open OpsPocket
2. Tap **Manage Cast.ai**
3. Paste your `CASTAI_API_KEY` and tap **Connect**
4. Browse your clusters
5. Tap a cluster to view details
6. Tap **Trigger Rebalancing** to optimize node configuration

### API Key Management

- Tap the key icon in the cluster list toolbar to change or delete your API key
- Keys can be rotated at any time - enter a new one and it replaces the old
- Deleting a key returns you to the home screen

### Rebalancing

The rebalancing flow matches the [Cast.ai rebalancing API](https://docs.cast.ai/docs/rebalancing):

1. Checks cluster status (must be "ready")
2. Creates a rebalancing plan with `minNodes: 1`
3. Waits 5 seconds for plan generation
4. Executes the plan

Clusters in hibernated, hibernating, failed, or other non-ready states are blocked from rebalancing with a dialog explaining why.

## Architecture

Clean Architecture with strict layer separation:

```
presentation/  ->  domain/  ->  data/
(UI + ViewModel)   (UseCases)   (API + Storage)
```

- **Presentation**: Jetpack Compose screens, Hilt ViewModels, sealed UI states, MVI pattern
- **Domain**: Use cases, domain models, repository interfaces
- **Data**: Retrofit API, kotlinx-serialization DTOs, OkHttp interceptors, EncryptedSharedPreferences

### Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Networking | Retrofit + OkHttp |
| Serialization | kotlinx-serialization |
| Navigation | Compose Navigation |
| State | StateFlow + MVI |
| Security | EncryptedSharedPreferences |
| Build | Gradle 8.11.1 (Kotlin DSL) |
| CI | GitHub Actions |
| Min SDK | 34 (Android 14) |
| Target SDK | 35 (Android 15) |

## CI/CD

- **CI** (`build.yml`): Runs unit tests and builds the APK on every push/PR to `main`
- **Release** (`release.yml`): Creates a GitHub Release with the APK when a version tag is pushed (`v*`), or on manual trigger via `workflow_dispatch`

To create a release:
```bash
git tag v0.1.0
git push origin v0.1.0
```

## Roadmap

- [ ] Azure integration
- [ ] Cluster node list view
- [ ] Rebalancing history
- [ ] Push notifications for rebalancing status
- [ ] Biometric authentication for API key access
- [ ] Multiple API key profiles
- [ ] Backend proxy for API key security

## License

Proprietary - All rights reserved.
