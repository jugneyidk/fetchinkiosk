# Project Map

This file is the navigable map for Fetchin Kiosk. Keep it synchronized with real files.

## Root Tree

```text
.
├── AGENTS.md
├── README.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── gradle/libs.versions.toml
├── gradle/wrapper/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/fetchin/kiosk/
│       │   └── res/
│       ├── test/
│       └── androidTest/
└── docs/
    ├── adr/
    ├── ACCEPTANCE_CRITERIA.md
    ├── ARCHITECTURE.md
    ├── DEVICE_PROVISIONING.md
    ├── HANDOFF_ANTIGRAVITY.md
    ├── IMPLEMENTATION_PLAN.md
    ├── OPEN_QUESTIONS.md
    ├── PROJECT_MAP.md
    ├── PROJECT_STATUS.md
    ├── REVIEW_CHECKLIST.md
    ├── ROADMAP.md
    └── SECURITY_MODEL.md
```

## Root Files

| Path | Purpose |
| --- | --- |
| `AGENTS.md` | Binding rules for future agents. |
| `README.md` | Human entry point. |
| `settings.gradle.kts` | Gradle plugin repositories and included modules. |
| `build.gradle.kts` | Root Gradle plugin declarations. |
| `gradle.properties` | Gradle and AndroidX flags. |
| `gradle/libs.versions.toml` | Central dependency versions. |
| `gradlew`, `gradlew.bat` | Gradle wrapper entry points. |
| `gradle/wrapper/` | Gradle wrapper jar and distribution configuration. |

## App Structure

| Path | Purpose |
| --- | --- |
| `app/build.gradle.kts` | Android app module configuration. |
| `app/src/main/AndroidManifest.xml` | App permissions, main activity, device admin receiver. |
| `app/src/main/java/com/fetchin/kiosk/MainActivity.kt` | UI coordinator and lifecycle entry point. |
| `app/src/main/java/com/fetchin/kiosk/admin/` | Administrative access and DeviceAdminReceiver. |
| `app/src/main/java/com/fetchin/kiosk/config/` | Central app configuration. |
| `app/src/main/java/com/fetchin/kiosk/kiosk/` | Lock Task and Device Owner orchestration. |
| `app/src/main/java/com/fetchin/kiosk/security/` | Security logging and future security helpers. |
| `app/src/main/java/com/fetchin/kiosk/ui/` | UI state model. |
| `app/src/main/java/com/fetchin/kiosk/util/` | Platform utility abstractions. |
| `app/src/main/java/com/fetchin/kiosk/web/` | WebView configuration, client, and URL policy. |
| `app/src/test/java/com/fetchin/kiosk/web/` | Unit tests for URL policy. |

## Generated And Local Files

| Path | Purpose |
| --- | --- |
| `local.properties` | Local SDK path for this workstation; ignored by `.gitignore`. |
| `.gradle/`, `build/`, `app/build/` | Generated Gradle/build outputs; ignored by `.gitignore`. |

## Main Classes

| Class | Status | Responsibility |
| --- | --- | --- |
| `MainActivity` | Skeleton | Coordinates UI, WebView setup, back handling, immersive mode. |
| `AppConfig` | Skeleton | Centralizes provisional start URL, hosts, flags, and admin gesture values. |
| `UrlPolicy` | Implemented | Validates HTTPS and allowlisted host boundaries, returning allow/block reasons. |
| `WebViewConfigurator` | Skeleton | Applies initial secure WebView settings. |
| `SecureWebViewClient` | Implemented initial | Blocks disallowed top-level navigation and returns 403 for disallowed subresources. |
| `KioskController` | Skeleton | Starts/stops Lock Task when permitted. |
| `DeviceOwnerStatusProvider` | Skeleton | Reads Device Owner and Lock Task status. |
| `KioskDeviceAdminReceiver` | Skeleton | Device admin receiver for provisioning. |
| `AdminAccessController` | Skeleton | Tracks hidden gesture. |
| `AdminPinVerifier` | Interface | Defines future PIN verification boundary. |
| `KioskUiState` | Skeleton | Represents visual states. |
| `ConnectivityObserver` | Interface | Future connectivity abstraction. |
| `KioskLogger` | Skeleton | Future safe admin event logging boundary. |

## Startup Flow

```mermaid
sequenceDiagram
    participant Android
    participant MainActivity
    participant AppConfig
    participant WebViewConfigurator
    participant WebView
    participant KioskController
    Android->>MainActivity: launch
    MainActivity->>AppConfig: load provisional defaults
    MainActivity->>MainActivity: apply immersive and screen policy
    MainActivity->>WebViewConfigurator: configure WebView
    MainActivity->>WebView: load start URL
    MainActivity->>KioskController: startLockTaskIfAllowed
```

## WebView Flow

```text
Start URL -> WebView -> SecureWebViewClient -> UrlPolicy -> allow HTTPS allowlisted host, show blocked state for blocked main-frame navigation, or return 403 for blocked subresources
```

## Kiosk Entry Flow

```text
MainActivity -> KioskController -> DevicePolicyManager.isLockTaskPermitted -> startLockTask when allowed -> show not-provisioned path when not allowed
```

## Admin Exit Flow

Planned:

```text
Hidden gesture -> PIN dialog -> AdminPinVerifier -> maintenance unlock timer -> KioskController.stopLockTaskFromAdminFlow -> maintenance menu -> re-enter Lock Task
```

## Error Recovery Flow

Planned:

```text
WebView error or offline -> custom error state -> retry button -> connectivity check -> reload or rebuild WebView if needed
```

## Dependency Direction

```text
MainActivity -> config, kiosk, web, ui, admin
web -> ui
kiosk -> admin
config -> web
security -> Android Log only for now
```

## Configuration Location

- Compile-time defaults: `app/build.gradle.kts` BuildConfig fields.
- Runtime wrapper: `AppConfig`.
- Future device-local configuration: planned DataStore or managed configuration.
- Future remote configuration: out of MVP.

## Tests Location

- Unit tests: `app/src/test/java/`.
- Instrumented tests: `app/src/androidTest/java/`, currently empty.
