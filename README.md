# OpsPocket

Cloud infrastructure management from your Android device. Currently supports Cast.ai cluster management and rebalancing.

## Features

- **Cast.ai Integration** - Manage Kubernetes clusters via Cast.ai API
  - View all connected clusters with status, region, and provider info
  - Trigger cluster rebalancing with one tap
  - Real-time status indicators (cluster status, agent connectivity)
  - Pull-to-refresh cluster list
- **Secure API Key Storage** - Keys encrypted on device via EncryptedSharedPreferences
  - Rotate or delete keys at any time
  - Key validation before connecting
- **Azure (Coming Soon)** - Placeholder for future Azure integration
- **Material You** - Dynamic color theming on Android 12+
- **Dark Mode** - Full dark theme support

## Screenshots

*Coming soon*

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
# Clone the repository
git clone <repo-url>
cd opspocket

# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Run Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

## Usage

1. Open OpsPocket
2. Tap "Manage Cast.ai"
3. Paste your `CASTAI_API_KEY` and tap Connect
4. Browse your clusters
5. Tap a cluster to view details
6. Tap "Trigger Rebalancing" to optimize node configuration

### API Key Management

- Tap the key icon in the cluster list toolbar to change or delete your API key
- Keys can be rotated at any time - just enter a new one
- Deleting a key returns you to the home screen

### Rebalancing

The rebalancing feature mirrors the [Cast.ai rebalancing API](https://docs.cast.ai/):

1. Creates a rebalancing plan with `minNodes: 1`
2. Waits for plan generation
3. Executes the plan

Rebalancing is only available when the cluster status is "ready". Clusters in hibernated, hibernating, failed, or other non-ready states will show a blocking dialog.

## Architecture

Clean Architecture with three layers:

```
presentation/  ->  domain/  ->  data/
(UI + ViewModel)   (UseCases)   (API + Storage)
```

- **Presentation**: Jetpack Compose screens, Hilt ViewModels, sealed UI states
- **Domain**: Use cases, domain models, repository interfaces
- **Data**: Retrofit API, DTOs, OkHttp interceptors, EncryptedSharedPreferences

### Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Networking | Retrofit + OkHttp |
| Serialization | kotlinx-serialization |
| Navigation | Compose Navigation |
| State | StateFlow + MVI |
| Security | EncryptedSharedPreferences |
| Build | Gradle 8.11.1 (Kotlin DSL) |
| Min SDK | 34 (Android 14) |
| Target SDK | 35 (Android 15) |

## Cast.ai API Endpoints

| Endpoint | Method | Description |
|---|---|---|
| `/v1/kubernetes/external-clusters` | GET | List clusters |
| `/v1/kubernetes/external-clusters/{id}` | GET | Cluster details |
| `/v1/kubernetes/clusters/{id}/rebalancing-plans` | POST | Create rebalancing plan |
| `/v1/kubernetes/clusters/{id}/rebalancing-plans/{planId}/execute` | POST | Execute plan |

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
