# Acceptance Criteria

Criteria are binary. Each item is pass/fail.

## Build And Project

- The app compiles with documented command.
- Unit tests run with documented command.
- Lint runs with documented command or failure is documented.
- No secrets are committed.
- `PROJECT_MAP.md` matches real structure.
- `PROJECT_STATUS.md` shows real validation results.

## WebView

- App opens configured start URL.
- Only allowed HTTPS hosts load.
- `https://pos.example.com` is allowed.
- `https://sub.pos.example.com` is allowed when base host is allowed.
- `http://pos.example.com` is blocked.
- `https://pos.example.com.attacker.com` is blocked.
- `https://example.com` is blocked.
- `file://` is blocked.
- `content://` is blocked.
- `intent://` is blocked.
- `javascript:` is blocked.
- Multiple windows are disabled.
- Arbitrary downloads are blocked or not implemented.
- File access is disabled.
- Content access is disabled.

## UI

- App has initializing state.
- App has loading state.
- App has web content state.
- App has offline state.
- App has load error state.
- App has not-provisioned state.
- App has maintenance state.
- Retry action is controlled.
- Back button does not close app.

## Kiosk

- Lock Task starts when permitted.
- App reports when Lock Task is not permitted.
- App reports when device is not Device Owner.
- Common user cannot exit kiosk on provisioned device.
- Kiosk can be reactivated after admin maintenance.

## Admin

- Hidden gesture opens PIN challenge only.
- Hidden gesture alone cannot exit kiosk.
- Incorrect PIN cannot disable kiosk.
- PIN is not stored as plain text.
- Admin exit is temporary and logged safely.

## Security

- WebView debugging is disabled in release.
- Cleartext traffic is disabled in release.
- `addJavascriptInterface` is absent unless documented by ADR.
- Logs do not include credentials, cookies, PINs, tokens, or sensitive full URLs.
- Screenshots are blocked when configured.

## Provisioning

- ADB Device Owner provisioning commands are documented.
- Development and production provisioning differences are documented.
- Failure cases are documented.
