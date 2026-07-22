# Handoff To Antigravity Claude Sonnet

You are continuing Fetchin Kiosk implementation. Do not reinterpret the product goal. Follow `AGENTS.md` first.

## System Summary

Fetchin Kiosk is a native Android kiosk shell for tablets. It loads one authorized internal web system in WebView and must eventually enforce Device Owner plus managed Lock Task Mode.

## Exact Current State

- Android project skeleton exists.
- Kotlin/XML/ViewBinding stack selected.
- Single `app` module exists.
- `MainActivity` coordinates a minimal WebView shell.
- `UrlPolicy` has 13 unit tests and returns allow/block decisions with reasons.
- WebView settings are hardened at skeleton level.
- WebView top-level navigation and subresources are routed through `UrlPolicy`.
- Download attempts trigger blocked navigation state instead of downloading.
- Device admin receiver exists.
- Kiosk controller can attempt Lock Task only when permitted.
- Admin PIN is interface only.
- Device-local config and provisioning logic are not implemented.

## Finished

- Repository structure.
- Governing docs.
- Architecture docs.
- Security model.
- Implementation phases.
- ADRs.
- Initial Kotlin/XML skeleton.
- Initial URL policy tests.
- Gradle wrapper `9.6.1`.
- `assembleDebug`, `testDebugUnitTest`, and `lintDebug` passing on this workstation.

## Not Implemented

- Secure PIN verification.
- Admin maintenance UI.
- DevicePolicyManager lock task package allowlisting.
- Device owner status rendering.
- Connectivity observer.
- Full WebView network/load error handling.
- Renderer crash recovery.
- DataStore or managed config.
- Instrumented tests.

## Read First

1. `AGENTS.md`
2. `docs/PROJECT_STATUS.md`
3. `docs/ARCHITECTURE.md`
4. `docs/SECURITY_MODEL.md`
5. `docs/IMPLEMENTATION_PLAN.md`
6. `docs/ACCEPTANCE_CRITERIA.md`
7. `docs/OPEN_QUESTIONS.md`
8. `docs/adr/`

## Required Implementation Order

1. Fix any build/lint failures recorded in `PROJECT_STATUS.md`.
2. Add provisioning status UI for `KIOSK-032`.
3. Implement connectivity/error handling for `KIOSK-050` and `KIOSK-051`.
5. Implement admin gesture plus secure PIN design for `KIOSK-040` and `KIOSK-041`.
6. Implement Lock Task package setup for Device Owner devices.
7. Add instrumented tests and manual validation notes.

## Decisions Not To Change Without ADR

- Native Android app.
- Kotlin.
- XML Views over Compose.
- Single `app` module for MVP.
- WebView allowlist and HTTPS-only policy.
- No JavaScript bridge by default.
- No plain-text PIN or embedded secrets.
- Device Owner required for real kiosk security.

## Known Risks

- SDK initially had only `android-36.1`; validation installed clean `android-36` after repairing an incomplete concurrent install.
- Real tablet model is unknown.
- Production URL and domains are unknown.
- PIN strategy is not selected.
- Device Owner setup needs factory-reset device.

## Key Files

- `app/src/main/java/com/fetchin/kiosk/MainActivity.kt`
- `app/src/main/java/com/fetchin/kiosk/config/AppConfig.kt`
- `app/src/main/java/com/fetchin/kiosk/web/UrlPolicy.kt`
- `app/src/main/java/com/fetchin/kiosk/web/WebViewConfigurator.kt`
- `app/src/main/java/com/fetchin/kiosk/web/SecureWebViewClient.kt`
- `app/src/main/java/com/fetchin/kiosk/kiosk/KioskController.kt`
- `app/src/main/java/com/fetchin/kiosk/admin/AdminPinVerifier.kt`
- `app/src/test/java/com/fetchin/kiosk/web/UrlPolicyTest.kt`

## Commands

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```

## First Recommended Task

Run the documented validation commands again in Antigravity. If they fail, fix the smallest build compatibility issue first and update `docs/PROJECT_STATUS.md` before feature work.

## Checklist Before Editing

- Read required docs.
- Check `PROJECT_STATUS.md`.
- Identify exact phase/task ID.
- Avoid new dependencies unless justified.
- Do not weaken security to pass tests.

## Checklist Before Finishing A Phase

- Build command run or failure documented.
- Unit tests run or failure documented.
- Lint run or failure documented.
- Docs updated.
- `PROJECT_MAP.md` updated if files changed.
- `PROJECT_STATUS.md` updated with real results.
- Security model unchanged or updated with approved rationale.

## Expected Report Format

- Task IDs completed.
- Files changed.
- Validation commands and results.
- Security impact.
- Open risks.
- Next task.
