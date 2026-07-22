# Project Status

| Field | Value |
| --- | --- |
| Updated | 2026-07-22 |
| Stage | Secure PIN verification boundary implementation started |
| Last change | Completed initial KIOSK-041 PBKDF2 admin PIN verifier with no embedded PIN |
| Next task | Implement KIOSK-042 maintenance mode screen and controlled admin session |
| Build status | Passing: `assembleDebug` |
| Test status | Passing: `testDebugUnitTest`, 20 tests |
| Lint status | Passing: `lintDebug` |

## Phase Progress

| Phase | Approximate Progress |
| --- | --- |
| Phase 0 Preparation | 100% |
| Phase 1 Shell Visual | 35% |
| Phase 2 Secure WebView | 70% |
| Phase 3 Kiosk Control | 55% |
| Phase 4 Admin Access | 55% |
| Phase 5 Resilience | 70% |
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

- `.\gradlew.bat testDebugUnitTest --console=plain` passed in 40s after KIOSK-041 changes.
- `.\gradlew.bat assembleDebug --console=plain` passed in 5s after KIOSK-041 changes.
- `.\gradlew.bat lintDebug --console=plain` passed in 11s after KIOSK-041 changes.
- `UrlPolicyTest` ran 13 tests, 0 failures, 0 errors.
- `AdminAccessControllerTest` ran 3 tests, 0 failures, 0 errors.
- `Pbkdf2AdminPinVerifierTest` ran 4 tests, 0 failures, 0 errors.

## Commands Failing

- Initial `.\gradlew.bat assembleDebug`, `.\gradlew.bat testDebugUnitTest`, and `.\gradlew.bat lintDebug` failed before `local.properties` existed because Gradle could not find Android SDK.
- A parallel validation attempt caused AGP to install `android-36` concurrently and left that SDK platform incomplete.
- Temporary `compileSdkExtension = 20` and `compileSdkPreview = "36.1"` experiments failed and were reverted.
- During KIOSK-021, first `testDebugUnitTest` run failed because `UrlPolicyTest.blocksInvalidUrl` expected `MissingScheme`; actual `java.net.URI` behavior for `not a url` is invalid URI. Test expectation was corrected to `Invalid`.

## Blockers

- No current validation blockers.

## Risks

- Production URL and domains are unknown.
- Target tablet model is unknown.
- Device Owner behavior varies by OS/OEM.
- Admin PIN uses configurable PBKDF2 hash/salt, but production config source is not selected.
- Real production hosts may need additional allowlist entries for subresources if the web system loads assets from dedicated domains.
- Connectivity uses Android validated internet capability, so captive portals and networks without validation are treated as offline.
- Renderer death is handled by recreating the WebView, but still needs manual validation on a real tablet.
- Hidden admin gesture now opens a PIN dialog; maintenance exit/session handling remains pending.

## Local Repair Notes

- `local.properties` was created for this workstation with the SDK path and is ignored by `.gitignore`.
- The incomplete SDK folder `platforms/android-36` was moved out of the SDK to `C:\Users\tokyo\AppData\Local\Temp\opencode\android-36.corrupt-20260712_194727` before AGP reinstalled `android-36` cleanly.
- Future Gradle validation should run sequentially, not in parallel, when SDK components may be installed.

## Notes

Foundation validation is complete. Next changes should update this file with fresh command results.
