# Test Strategy

Testing must prove both app behavior and kiosk/security assumptions on real target tablets.

## Pyramid

| Layer | Purpose |
| --- | --- |
| Unit tests | URL policy, admin gesture timing, configuration parsing. |
| Instrumented tests | WebView navigation, UI states, back behavior. |
| Manual device tests | Lock Task, Device Owner, physical buttons, OEM behavior. |
| Security review | Threat model checklist and abuse cases. |

## Unit Tests

- Allow exact HTTPS hosts.
- Allow valid subdomains.
- Block HTTP.
- Block lookalike suffix hosts.
- Block unknown hosts.
- Block `file`, `content`, `intent`, `javascript`, `market`, `tel`, `mailto`.
- Block empty and invalid URLs.
- Test admin gesture timeout and tap count.

## Instrumented Tests

- Launch Activity.
- Verify loading state.
- Verify retry button state.
- Verify back does not finish Activity.
- Verify disallowed navigation is blocked.
- Verify orientation policy.

## Manual Tests

- Factory-reset provisioning.
- Device Owner command success.
- Lock Task activation.
- User cannot leave app with navigation buttons.
- Notifications/settings access blocked as expected for managed device.
- Hidden gesture alone does not exit kiosk.
- Wrong PIN does not exit kiosk.
- Correct admin flow exits temporarily and re-enters.
- Reboot returns to intended mode.
- App process death recovers.

## Android Version Matrix

Minimum matrix before production:

| Android | Priority |
| --- | --- |
| Android 8.0/8.1 API 26/27 | Minimum support verification |
| Android 10/11 | Common legacy tablets |
| Android 12/13 | Midrange support |
| Android 14/15/16 | Current/future tablets |

## WebView Matrix

- Current Android System WebView.
- Manufacturer bundled WebView on target tablets.
- WebView after Play/System update.

## Network Tests

- Offline before launch.
- Offline during page load.
- Offline after content loaded.
- Slow page.
- Captive portal.
- Invalid certificate.
- DNS failure.
- Server redirects to disallowed host.

## Security Manual Tests

- Press back repeatedly.
- Press home/recent apps.
- Pull notification shade.
- Try `intent://`, `file://`, `content://`, `javascript:` links.
- Try popup/window open.
- Try download link.
- Rotate device if rotation is allowed.
- Attempt screenshot.
- Inspect logs for secrets.
- Decompile APK and confirm no real secrets/PIN.

## Update Tests

- Install update over existing debug build.
- Verify configuration remains valid.
- Verify Lock Task remains functional.
- Verify WebView session behavior is acceptable.
