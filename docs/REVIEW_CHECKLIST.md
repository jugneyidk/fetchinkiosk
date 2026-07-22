# Review Checklist

Use this checklist for final GPT-5.5 review after implementation.

## Architecture

- `MainActivity` delegates security decisions.
- URL policy is centralized and tested.
- WebView configuration is centralized.
- Kiosk control is isolated from UI details.
- Admin authorization is separate from hidden gesture detection.
- No premature modules or frameworks were added.

## Security

- No secrets in repository.
- No production PIN in source.
- No plain-text PIN storage.
- No TLS bypass.
- Cleartext traffic disabled.
- `FLAG_SECURE` policy matches requirements.
- Logs omit sensitive data.

## Gradle

- Dependencies declared through version catalog.
- Added dependencies are justified.
- Debug/release behavior differs correctly.
- WebView debugging disabled in release.
- Release minification decision documented.

## WebView

- HTTPS-only allowlist enforced.
- Host boundary logic prevents suffix attacks.
- Unsafe schemes blocked.
- File/content access disabled.
- Multiple windows disabled.
- Popups blocked.
- Downloads blocked or explicitly controlled.
- Errors show custom UI.

## Device Owner And Lock Task

- DeviceAdminReceiver component matches docs.
- Device Owner status checked.
- Lock Task package allowlisting implemented for owner mode.
- App clearly reports non-provisioned state.
- Screen pinning not presented as secure equivalent.

## Admin Flow

- Gesture alone never exits kiosk.
- PIN verifier uses approved secure strategy.
- Wrong PIN keeps kiosk active.
- Admin unlock is temporary.
- Re-entry to kiosk works.
- Admin events are logged safely.

## UI And Errors

- All documented states render.
- Back button does not exit app.
- Offline state works.
- Retry is controlled.
- Orientation behavior matches decision.
- Accessibility basics are acceptable.

## Tests

- Unit tests pass.
- URL policy tests cover unsafe cases.
- Instrumented tests cover Activity/WebView basics.
- Manual Device Owner tests recorded.
- Update tests recorded.

## Documentation

- `PROJECT_MAP.md` matches actual structure.
- `PROJECT_STATUS.md` has current real results.
- `OPEN_QUESTIONS.md` reflects unresolved owner decisions.
- ADRs exist for changed architecture/security decisions.
- `DEVICE_PROVISIONING.md` matches package names and build types.

## APK

- Debug APK install tested.
- Release APK build tested when signing is ready.
- No sensitive data visible in decompiled resources/build config.
