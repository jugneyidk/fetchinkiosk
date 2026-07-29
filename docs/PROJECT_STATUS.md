# Project Status

| Field | Value |
| --- | --- |
| Updated | 2026-07-22 |
| Stage | First-run setup implementation complete |
| Last change | Added local first-run URL and administrator PIN setup for public APK use |
| Next task | Manually verify setup save on emulator/device, then commit if approved |
| Build status | Passing: `assembleDebug` |
| Test status | Passing: `testDebugUnitTest`, 25 tests |
| Lint status | Passing: `lintDebug` |

## Phase Progress

| Phase | Approximate Progress |
| --- | --- |
| Phase 0 Preparation | 100% |
| Phase 1 Shell Visual | 35% |
| Phase 2 Secure WebView | 70% |
| Phase 3 Kiosk Control | 70% |
| Phase 4 Admin Access | 85% |
| Phase 5 Resilience | 80% |
| Phase 6 Provisioning | 20% |
| Phase 7 Tests And Hardening | 10% |

## Environment Observed

| Item | Result |
| --- | --- |
| OS | Windows 10.0.19045 |
| JDK | Temurin OpenJDK 17.0.19 |
| Git repository | Initialized; worktree clean before KIOSK-032 changes |
| Global Gradle | Not installed |
| Gradle wrapper | 9.6.1 |
| Android Gradle Plugin | 9.2.1 |
| Android SDK | `C:\Users\tokyo\AppData\Local\Android\Sdk` |
| SDK platform | `android-36` installed during validation; `android-36.1` also present |
| Build tools | `36.0.0` installed during validation; `36.1.0`, `37.0.0` also present |
| ADB | 37.0.0 available |

## Commands Passing

- `.\gradlew.bat testDebugUnitTest --console=plain` passed in 53s after first-run setup changes.
- `.\gradlew.bat assembleDebug --console=plain` passed in 8s after first-run setup changes.
- `.\gradlew.bat lintDebug --console=plain` passed in 46s after first-run setup changes.
- `UrlPolicyTest` ran 13 tests, 0 failures, 0 errors.
- `AdminAccessControllerTest` ran 3 tests, 0 failures, 0 errors.
- `Pbkdf2AdminPinVerifierTest` ran 4 tests, 0 failures, 0 errors.
- `InitialSetupConfigBuilderTest` ran 5 tests, 0 failures, 0 errors.
- Emulator `Pixel_7a` accepted Device Owner provisioning with `adb shell dpm set-device-owner com.fetchin.kiosk.debug/com.fetchin.kiosk.admin.KioskDeviceAdminReceiver` after wiping data.
- Emulator `dumpsys activity activities` showed `mLockTaskModeState=LOCKED` and `u0:[com.fetchin.kiosk.debug]` after app launch.
- Emulator first-run setup screen displayed `Initial setup`, URL field, PIN field, confirmation field, and `START KIOSK` after `pm clear` and app launch.
- Emulator first-run setup correctly stayed out of Lock Task before configuration: `mLockTaskModeState=NONE`.

## Commands Failing

- Initial `.\gradlew.bat assembleDebug`, `.\gradlew.bat testDebugUnitTest`, and `.\gradlew.bat lintDebug` failed before `local.properties` existed because Gradle could not find Android SDK.
- A parallel validation attempt caused AGP to install `android-36` concurrently and left that SDK platform incomplete.
- Temporary `compileSdkExtension = 20` and `compileSdkPreview = "36.1"` experiments failed and were reverted.
- During KIOSK-021, first `testDebugUnitTest` run failed because `UrlPolicyTest.blocksInvalidUrl` expected `MissingScheme`; actual `java.net.URI` behavior for `not a url` is invalid URI. Test expectation was corrected to `Invalid`.
- First emulator Device Owner attempt failed with `Not allowed to set the device owner because there are already some accounts on the device`; wiping the AVD fixed it.
- First-run setup save-by-ADB-input validation was unreliable because emulator rotation/keyboard caused text to enter the wrong fields. Validate save manually or with an instrumented test.

## Blockers

- No current validation blockers.

## Risks

- Extra allowed sibling domains are not configurable yet; first-run setup allows the configured URL host and true subdomains only.
- Target tablet model is unknown.
- Device Owner behavior varies by OS/OEM.
- Admin PIN setup stores PBKDF2 hash/salt in private app data, not plain text.
- Real production hosts may need additional allowlist entries for subresources if the web system loads assets from dedicated domains.
- Connectivity uses Android validated internet capability, so captive portals and networks without validation are treated as offline.
- Renderer death is handled by recreating the WebView, but still needs manual validation on a real tablet.
- Hidden admin gesture now opens a PIN dialog; valid PIN enters timed maintenance mode.
- Maintenance mode attempts controlled Lock Task stop after PIN verification and attempts Lock Task restart on return or timeout.
- Controlled Lock Task exit/re-entry still needs real Device Owner hardware validation.
- Device Owner builds attempt to allowlist their own package before starting Lock Task; this still needs real hardware validation.
- `adb shell input KEYCODE_HOME` can exit Lock Task in the emulator because it is an ADB/shell-level injected event; do not treat it as equivalent to a physical user pressing Home.
- Forgetting the PIN requires clearing app data, reinstalling, or factory reset/removing Device Owner on managed devices.

## Local Repair Notes

- `local.properties` was created for this workstation with the SDK path and is ignored by `.gitignore`.
- The incomplete SDK folder `platforms/android-36` was moved out of the SDK to `C:\Users\tokyo\AppData\Local\Temp\opencode\android-36.corrupt-20260712_194727` before AGP reinstalled `android-36` cleanly.
- Future Gradle validation should run sequentially, not in parallel, when SDK components may be installed.
- After wiping `Pixel_7a`, the first emulator boot temporarily appeared `offline` in ADB; killing the stuck emulator/QEMU process and relaunching with `-no-snapshot-load` recovered it.
- The same AVD later showed System UI ANR during visual first-run setup validation; relaunching without wipe and GPU software made UI dumping work again.

## Notes

Foundation validation is complete. Next changes should update this file with fresh command results.
