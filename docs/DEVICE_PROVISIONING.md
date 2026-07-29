# Device Provisioning

These commands are documentation only. Do not run provisioning commands on a real device without explicit owner approval.

## Development Tablet Preparation

1. Back up anything important.
2. Factory reset the tablet when testing Device Owner.
3. Do not add a Google account before Device Owner provisioning.
4. Enable Developer Options.
5. Enable USB debugging.
6. Connect by USB and accept the debugging prompt.

## Verify ADB

```powershell
adb devices
```

Expected: one authorized device.

## Install Development APK

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Set Device Owner

Package name is provisional. Debug builds use `com.fetchin.kiosk.debug`; release builds use `com.fetchin.kiosk`.

Debug command:

```powershell
adb shell dpm set-device-owner com.fetchin.kiosk.debug/com.fetchin.kiosk.admin.KioskDeviceAdminReceiver
```

Release command:

```powershell
adb shell dpm set-device-owner com.fetchin.kiosk/com.fetchin.kiosk.admin.KioskDeviceAdminReceiver
```

## Verify Device Owner

```powershell
adb shell dpm get-device-owner
```

## Start App

Debug:

```powershell
adb shell monkey -p com.fetchin.kiosk.debug 1
```

Release:

```powershell
adb shell monkey -p com.fetchin.kiosk 1
```

On first launch, enter the HTTPS URL and administrator PIN. Kiosk mode starts after setup is saved.

## Reset First-Run Setup

During development on an unmanaged install:

```powershell
adb shell pm clear com.fetchin.kiosk.debug
```

On a Device Owner device, Android may block normal uninstall/reset flows. Remove Device Owner or factory reset the development tablet if app data cannot be cleared safely.

## Exit Kiosk During Development

Final admin exit flow is not implemented yet. During development, use ADB only on authorized devices.

```powershell
adb shell am force-stop com.fetchin.kiosk.debug
```

## Remove Device Owner During Development

Android may require factory reset depending on state and OS version. Try only on development devices:

```powershell
adb shell dpm remove-active-admin com.fetchin.kiosk.debug/com.fetchin.kiosk.admin.KioskDeviceAdminReceiver
```

If removal fails, factory reset the development tablet.

## Common Failures

| Error | Cause | Action |
| --- | --- | --- |
| `Not allowed to set the device owner` | Device already provisioned or account exists | Factory reset, skip accounts, retry. |
| `Unknown admin` | APK not installed or component mismatch | Confirm package/component names. |
| Device unauthorized | USB prompt not accepted | Reconnect, accept prompt, retry. |
| Lock Task does not start | App not allowlisted | Implement DevicePolicyManager lock task package setup. |

## Emulator Validation Notes

- A clean emulator can be used for early Device Owner validation.
- If `set-device-owner` fails because accounts exist, wipe the AVD and retry before adding any account.
- Debug package component is `com.fetchin.kiosk.debug/com.fetchin.kiosk.admin.KioskDeviceAdminReceiver`.
- After launching the app, `adb shell dumpsys activity activities` should show `mLockTaskModeState=LOCKED` and `u0:[com.fetchin.kiosk.debug]` under `mLockTaskPackages`.
- `adb shell input KEYCODE_HOME` is not a physical-user security test because shell-injected input can bypass conditions that normal users cannot.

## Development Vs Production

| Area | Development | Production |
| --- | --- | --- |
| Build | Debug allowed | Release only |
| WebView debugging | Allowed | Disabled |
| Device Owner | Optional for UI work | Required |
| Reset policy | Frequent factory reset acceptable | Controlled enrollment process |
| Config | First-run local setup | Managed, audited values or controlled first-run setup |

## Updates

- Use `adb install -r` for development updates.
- Production updates should use MDM/EMM or controlled maintenance process.
- Do not uninstall app casually from Device Owner devices.
- Test updates without losing kiosk policy before rollout.
