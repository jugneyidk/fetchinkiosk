# Release Guide

This guide describes how to prepare APK releases for Fetchin Kiosk. Do not publish a release until the APK has been validated on the intended Android version and, for production use, on real target hardware.

## Release Checklist

- [ ] `README.md` explains install, setup, provisioning, and reset behavior.
- [ ] `docs/PROJECT_STATUS.md` shows current validation results.
- [ ] No production URL, PIN, token, certificate, API key, or signing secret is committed.
- [ ] `testDebugUnitTest`, `assembleDebug`, and `lintDebug` pass or failures are documented.
- [ ] APK was manually launched and first-run setup was checked.
- [ ] Device Owner and Lock Task were validated on emulator.
- [ ] Real tablet validation completed before production claims.

## Build Debug APK

```powershell
.\gradlew.bat assembleDebug --console=plain
```

Debug APK path:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Debug APKs are useful for development releases only. They include WebView debugging and package name `com.fetchin.kiosk.debug`.

## Build Release APK

Release signing is configured through a local `keystore.properties` file. This file is ignored by Git and must never be committed.

## Create A Local Signing Key

Generate a local keystore outside version control:

```powershell
keytool -genkeypair -v -keystore release-key.jks -alias fetchin-kiosk -keyalg RSA -keysize 2048 -validity 10000
```

Copy the example file:

```powershell
Copy-Item keystore.properties.example keystore.properties
```

Edit `keystore.properties` locally:

```properties
storeFile=release-key.jks
storePassword=your-keystore-password
keyAlias=fetchin-kiosk
keyPassword=your-key-password
```

Do not commit `keystore.properties` or `*.jks` files.

Build the release APK:

```powershell
.\gradlew.bat assembleRelease --console=plain
```

Signed release APK path when `keystore.properties` exists:

```text
app\build\outputs\apk\release\app-release.apk
```

## GitHub Release Draft

Use a short version title:

```text
Fetchin Kiosk v0.1.0
```

Suggested release notes:

```markdown
## What's Included

- First-run HTTPS URL and administrator PIN setup.
- WebView URL allowlist and unsafe scheme blocking.
- Device Owner and Lock Task support.
- Hidden admin PIN flow for temporary maintenance.

## Install

Download the APK and install it on an Android device.

## Security Notes

- Real kiosk security requires Device Owner provisioning.
- Development fallback mode is not secure kiosk mode.
- No production URL or PIN is embedded in the APK.

## Validation

- Unit tests: PASS
- Debug build: PASS
- Lint: PASS
- Emulator Device Owner startup: PASS
- Real tablet validation: pending
```

## Do Not Release If

- APK contains hardcoded production URL or PIN.
- Release signing secrets are in the repo.
- `addJavascriptInterface` was added without ADR and threat model.
- Cleartext traffic was enabled for production.
- Real kiosk security is claimed without Device Owner validation.
