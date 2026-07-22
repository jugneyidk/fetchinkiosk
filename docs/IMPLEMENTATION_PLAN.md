# Implementation Plan

Each task must end with validation and documentation updates. Status values: pending, in progress, completed.

## Phase 0: Preparation

| Task | Objective | Files | Dependencies | Done | Tests | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| KIOSK-001 | Create Android project skeleton | Gradle files, `app/` | JDK, SDK, Gradle | Project syncs and builds | `assembleDebug` passed | SDK/Gradle mismatch | completed |
| KIOSK-002 | Add governing docs | `AGENTS.md`, `docs/` | None | Required docs exist | Manual doc review | Drift from code | completed |
| KIOSK-003 | Add URL policy unit tests | `UrlPolicyTest` | JUnit | Cases pass | `testDebugUnitTest` passed, 11 tests | Incomplete host rules | completed |

## Phase 1: Shell Visual

| Task | Objective | Files | Dependencies | Done | Tests | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| KIOSK-010 | Implement base Activity | `MainActivity`, layout | ViewBinding | States render | Manual launch | Activity grows too much | completed |
| KIOSK-011 | Refine visual states | `ui`, layout | None | All states visible | Screenshot/manual | Poor accessibility | pending |
| KIOSK-012 | Fullscreen immersive behavior | `MainActivity` | Android APIs | System bars hidden | Manual device test | Not secure alone | completed |

## Phase 2: Secure WebView

| Task | Objective | Files | Dependencies | Done | Tests | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| KIOSK-020 | Harden WebView settings | `WebViewConfigurator` | WebView | Unsafe access disabled | Unit/manual | Web app compatibility | completed initial |
| KIOSK-021 | Complete navigation blocking | `SecureWebViewClient` | `UrlPolicy` | Top-level and subresource loads pass policy | Unit tests passed | Subresource allowlist may need real host tuning | completed |
| KIOSK-022 | Add blocked navigation UI | `web`, `ui` | UI states | User sees safe blocked navigation state | Build/lint passed | Copy and UX need device review | completed initial |
| KIOSK-023 | Disable downloads | `web` | WebView APIs | Download listener blocks with safe UI state | Build/lint passed | Needed business flow | completed initial |

## Phase 3: Kiosk Control

| Task | Objective | Files | Dependencies | Done | Tests | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| KIOSK-030 | Implement DeviceAdminReceiver | `admin` | Manifest | Receiver registered | Manual/ADB | Confusing admin vs owner | completed initial |
| KIOSK-031 | Configure Lock Task packages | `KioskController` | Device Owner | App allowlisted | Device test | Requires factory reset | pending |
| KIOSK-032 | Show provisioning status | `kiosk`, `ui` | DevicePolicyManager | Device Owner and Lock Task permission are visible when not provisioned | Unit/build validated | Needs real-device validation | completed initial |
| KIOSK-033 | Add development fallback messaging | `ui` | None | Debug fallback banner clearly says kiosk security is inactive | Unit/build validated | Must not ship as production assurance | completed initial |

## Phase 4: Administrative Access

| Task | Objective | Files | Dependencies | Done | Tests | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| KIOSK-040 | Wire hidden gesture | `MainActivity`, `admin` | UI | Gesture opens PIN only | Unit/manual | Discoverability vs abuse | pending |
| KIOSK-041 | Implement secure PIN verifier | `admin`, `security` | Keystore/DataStore or remote | No plain PIN | Unit/security review | Weak derivation | pending |
| KIOSK-042 | Maintenance mode screen | `ui`, layout | Admin flow | Timed admin mode | Manual | User escapes too long | pending |
| KIOSK-043 | Re-enter kiosk | `kiosk`, `admin` | Lock Task | Kiosk restores | Device test | Lock Task failure | pending |

## Phase 5: Resilience

| Task | Objective | Files | Dependencies | Done | Tests | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| KIOSK-050 | Connectivity observer | `util`, `ui` | ConnectivityManager | Offline state checks validated internet before loading/retry | Build/lint validated | Captive portals may appear offline by design | completed initial |
| KIOSK-051 | WebView error handling | `web`, `ui` | WebViewClient callbacks | Main-frame network, HTTP, and TLS errors show custom error state | Build/lint validated | Needs instrumented WebView tests | completed initial |
| KIOSK-052 | Renderer crash recovery | `web` | API support | WebView can recover | Manual | Data loss | pending |
| KIOSK-053 | Minimal persistence | `config` | DataStore if needed | Config survives reboot | Unit/manual | Local tampering | pending |

## Phase 6: Provisioning

| Task | Objective | Files | Dependencies | Done | Tests | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| KIOSK-060 | Document ADB provisioning | `DEVICE_PROVISIONING.md` | ADB | Commands documented | Review | Device wipe required | completed |
| KIOSK-061 | Validate on real tablet | Docs, code | Hardware | Provisioning works | Device test | OEM differences | pending |
| KIOSK-062 | Plan QR provisioning | `ROADMAP.md` | Android provisioning | Path documented | Review | Future scope creep | pending |

## Phase 7: Tests And Hardening

| Task | Objective | Files | Dependencies | Done | Tests | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| KIOSK-070 | Expand URL tests | `UrlPolicyTest` | JUnit | Edge cases covered | Unit | IDN/punycode gaps | pending |
| KIOSK-071 | Add instrumented WebView tests | `androidTest` | Emulator/device | Navigation tested | Espresso | Flaky network | pending |
| KIOSK-072 | Manual security checklist | `REVIEW_CHECKLIST.md` | Real device | Checklist passed | Manual | Human error | pending |
| KIOSK-073 | Release hardening | Gradle, docs | Signing process | Release safe | Lint/review | Signing secrets | pending |
