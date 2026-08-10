# Fetchin Kiosk

<p align="center">
  <img src="logofetchinkiosk.png" alt="Fetchin Kiosk logo" width="160" />
</p>

Fetchin Kiosk turns an Android tablet into a dedicated WebView kiosk for one HTTPS web system. It is built for public APK distribution: each installation configures its own page URL and administrator PIN on first launch, so the repository does not ship with a production URL, password, token, or secret.

## What It Does

| Area | Behavior |
| --- | --- |
| First-run setup | Collects HTTPS URL, admin PIN, and PIN confirmation before kiosk mode starts. |
| WebView lock-down | Allows only the configured HTTPS host and true subdomains. Blocks unsafe schemes and downloads. |
| Kiosk mode | Uses Android Device Owner, DevicePolicyManager, and Lock Task Mode when provisioned. |
| Admin access | Hidden gesture opens PIN challenge; valid PIN starts a temporary maintenance session. |
| PIN storage | Stores PBKDF2 hash and salt material in private app data, never plain-text PIN. |
| Public repo safety | No production URL, PIN, API key, token, certificate, or shared secret belongs in source. |

## Current Status

Fetchin Kiosk is an MVP-stage native Android app. The core WebView policy, first-run setup, admin PIN verification, Device Owner/Lock Task startup, and emulator validation are implemented. Real-device validation is still required before production use.

## Quick Start

| Goal | Path |
| --- | --- |
| Try the app without kiosk security | Install APK, launch app, complete first-run setup. |
| Test real kiosk behavior in emulator | Wipe AVD, install APK, set Device Owner, launch app, verify Lock Task. |
| Build from source | Run `assembleDebug`, install `app-debug.apk`, then complete setup. |
| Prepare public release | Follow `docs/RELEASE_GUIDE.md`; do not claim production security without real tablet validation. |

## Security In One Minute

Real kiosk security requires **Device Owner + Lock Task Mode**. Immersive mode, fullscreen UI, screen pinning, or hiding Android navigation is NOT enough.

Development builds may show this warning:

```text
Development fallback: kiosk security is not active.
Device Owner: no
Lock Task permitted: no
```

That means Android can still show notifications, launcher, gestures, and system UI. It is expected until the device is provisioned as Device Owner.

## Install From APK

No public release APK is attached yet. When releases start, download the latest APK from the GitHub Releases page and install it on the target Android device.

For local development builds:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## First Run Setup

On first launch, enter:

- HTTPS page URL, for example `https://example.com`.
- Administrator PIN.
- PIN confirmation.

After saving setup, the app stores the URL and PBKDF2 PIN material in private app data, then starts the configured kiosk web page.

To reset setup on an unmanaged development install:

```powershell
adb shell pm clear com.fetchin.kiosk.debug
```

On a Device Owner device, Android may block normal app data clearing or uninstall flows. If the administrator PIN is forgotten, you may need to remove Device Owner or factory reset the tablet.

## Build From Source

Requirements:

| Tool | Version |
| --- | --- |
| JDK | 17 |
| Gradle wrapper | 9.6.1, included |
| Android Gradle Plugin | 9.2.1 |
| Compile SDK | 36 |
| Minimum SDK | 26 |

Build debug APK:

```powershell
.\gradlew.bat assembleDebug --console=plain
```

Run unit tests:

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
```

Run lint:

```powershell
.\gradlew.bat lintDebug --console=plain
```

## Device Owner Setup

Device Owner provisioning usually requires a clean factory-reset device with no personal account configured.

Debug build component:

```powershell
adb shell dpm set-device-owner com.fetchin.kiosk.debug/com.fetchin.kiosk.admin.KioskDeviceAdminReceiver
```

Release build component:

```powershell
adb shell dpm set-device-owner com.fetchin.kiosk/com.fetchin.kiosk.admin.KioskDeviceAdminReceiver
```

Launch debug build:

```powershell
adb shell am start -n com.fetchin.kiosk.debug/com.fetchin.kiosk.MainActivity
```

Verify Lock Task:

```powershell
adb shell dumpsys activity activities | findstr /C:"mLockTaskModeState" /C:"mLockTaskPackages"
```

Expected provisioned state:

```text
mLockTaskModeState=LOCKED
u0:[com.fetchin.kiosk.debug]
```

## Emulator Testing

The Android emulator can validate the basic Device Owner and Lock Task flow, but final confidence still requires target hardware.

Useful emulator flow:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell dpm set-device-owner com.fetchin.kiosk.debug/com.fetchin.kiosk.admin.KioskDeviceAdminReceiver
adb shell am start -n com.fetchin.kiosk.debug/com.fetchin.kiosk.MainActivity
adb shell dumpsys activity activities | findstr /C:"mLockTaskModeState" /C:"mLockTaskPackages"
```

If `set-device-owner` fails because accounts exist, wipe the AVD and retry before adding any account.

Important emulator caveat: `adb shell input KEYCODE_HOME` is shell-injected input and can bypass behavior that a normal physical user cannot. Do not treat that as final kiosk escape validation.

## Reset And Recovery

| Situation | Recovery |
| --- | --- |
| Wrong URL during development | Clear app data or reinstall. |
| Forgotten PIN on unmanaged install | Clear app data or reinstall. |
| Forgotten PIN on Device Owner tablet | Remove Device Owner if possible, or factory reset. |
| Need to update APK | Use `adb install -r` during development; use controlled release process for production. |

## Architecture

| Package | Responsibility |
| --- | --- |
| `config` | First-run setup, local config, runtime `AppConfig`. |
| `web` | WebView settings, navigation policy, load/error handling. |
| `kiosk` | Device Owner status and Lock Task control. |
| `admin` | Hidden gesture, Device Admin receiver, PIN verifier. |
| `ui` | UI state model. |
| `security` | Safe event logging boundary. |

Detailed docs:

- `AGENTS.md` governs future agents and project rules.
- `docs/ARCHITECTURE.md` explains structure and flow.
- `docs/SECURITY_MODEL.md` documents threats and mitigations.
- `docs/DEVICE_PROVISIONING.md` lists ADB provisioning commands.
- `docs/RELEASE_GUIDE.md` describes APK release preparation.
- `docs/PROJECT_STATUS.md` tracks current validation state.

## Current Limitations

- First-run setup supports one configured URL host and true subdomains. Extra sibling API/CDN hosts need future setup UI if required.
- Real target tablet validation is still required.
- WebView renderer recovery needs real tablet validation.
- No MDM/EMM, QR provisioning, printing, camera, downloads, or file uploads yet.
- Release signing and GitHub APK publishing are not configured yet.

## Contributing

Read `AGENTS.md` before changing code. Security-sensitive behavior must stay synchronized with `docs/SECURITY_MODEL.md` and `docs/ARCHITECTURE.md`.

Before handing off changes, run or document why you could not run:

```powershell
.\gradlew.bat assembleDebug --console=plain
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat lintDebug --console=plain
```
