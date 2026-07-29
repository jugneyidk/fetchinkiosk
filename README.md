# Fetchin Kiosk

Fetchin Kiosk is a native Android kiosk shell that will turn a tablet into a dedicated device for one authorized internal web system.

## Current Status

Foundation stage. The repository contains a native Android kiosk shell with first-run setup, WebView URL policy, admin PIN verification, Device Owner/Lock Task support, tests, and architecture/security documentation.

## Problem Solved

Opening the internal system in Chrome allows users to leave the intended workflow, open other sites, switch apps, access settings, or use the tablet for unrelated tasks. Fetchin Kiosk will replace that with a managed native app using WebView, Device Owner, DevicePolicyManager, and Lock Task Mode.

## Stack

| Area | Choice |
| --- | --- |
| Platform | Native Android |
| Language | Kotlin |
| UI | XML Views with ViewBinding |
| Build | Gradle Kotlin DSL |
| Architecture | Single `app` module, package-by-responsibility |
| Minimum SDK | API 26, provisional |
| Compile/Target SDK | API 36, provisional based on installed SDK |
| Web runtime | Android WebView, not Chrome Custom Tabs |

## Requirements

- JDK 17.
- Android SDK platform compatible with compile SDK 36.
- Android SDK Build Tools.
- Gradle wrapper, or local Gradle able to run this project.
- A factory-reset Android tablet for real Device Owner provisioning.

## Open Project

1. Open repository root in Android Studio or compatible IDE.
2. Let Gradle sync download declared dependencies.
3. Read `AGENTS.md` before changing code.

## Build

Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

The repository includes Gradle wrapper `9.6.1`.

Fallback when wrapper is not available:

```powershell
gradle assembleDebug
```

## Test

```powershell
.\gradlew.bat testDebugUnitTest
```

## Lint

```powershell
.\gradlew.bat lintDebug
```

## Install Development APK

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## First Run Setup

On first launch, the app asks for:

- HTTPS page URL.
- Administrator PIN.
- PIN confirmation.

The URL is stored in private app data. The PIN is never stored as plain text; the app stores PBKDF2 hash and salt material. Kiosk mode starts only after setup is saved.

To reset setup during development, clear app data or reinstall the APK. If the app is already Device Owner on a managed tablet, Android may require removing Device Owner or factory resetting the device before uninstalling.

## Limitations Now

- First-run setup supports one configured URL host; extra sibling domains need future configuration UI if a web app needs them.
- Controlled Lock Task exit/re-entry needs real Device Owner hardware validation.
- WebView renderer recovery needs real tablet validation.
- No remote configuration, MDM, QR provisioning, printing, camera, downloads, or file uploads.

## Internal Docs

- `AGENTS.md` governs future agents.
- `docs/PROJECT_MAP.md` maps files and flows.
- `docs/ARCHITECTURE.md` defines structure.
- `docs/SECURITY_MODEL.md` defines threats and mitigations.
- `docs/IMPLEMENTATION_PLAN.md` defines phases.
- `docs/DEVICE_PROVISIONING.md` documents ADB provisioning.
- `docs/HANDOFF_ANTIGRAVITY.md` directs the next Claude Sonnet agent.

## Device Owner Warning

Device Owner normally requires a factory-reset or specifically provisioned device. Immersive mode and screen pinning are not a secure replacement for managed Lock Task Mode.
