# Security Model

Fetchin Kiosk security depends on native controls plus managed provisioning. A WebView alone is not kiosk security.

## Protected Assets

- Tablet operating system access.
- Internal system session.
- Application configuration.
- Administrative unlock flow.
- Allowed domain list.
- Local device state.
- Operational logs.

## Actors

| Actor | Description |
| --- | --- |
| Common user | Employee, customer, or operator using the tablet. |
| Local administrator | Authorized person allowed to exit kiosk temporarily. |
| Device owner admin | Organization provisioning tablets. |
| Web system | Authorized internal web application. |
| Attacker | User or page attempting escape, exfiltration, or misuse. |

## Attack Surface

- Android navigation buttons and gestures.
- Notifications and system UI.
- Physical buttons and reboot.
- WebView navigation.
- External URL schemes.
- JavaScript execution.
- Cookies and DOM storage.
- Admin gesture and PIN flow.
- Device provisioning commands.
- APK decompilation.
- Logs.

## Threats And Mitigations

| Threat | Mitigation |
| --- | --- |
| User leaves app through Android UI | Device Owner and Lock Task Mode. |
| User opens external site | `UrlPolicy` HTTPS host allowlist. |
| Page opens external scheme | Block schemes by default. |
| Page opens popup | Disable multiple windows and automatic windows. |
| File exfiltration through WebView | Disable file/content access. |
| Mixed-content downgrade | Block mixed content. |
| PIN extracted from APK | No plain PIN in code; use derived hash/Keystore/remote validation. |
| Logs leak secrets | Log event types only, no sensitive values. |
| Screenshot leaks internal data | `FLAG_SECURE` by default. |
| Device reboot exits kiosk | Boot behavior and launcher policy must be implemented in later phase. |
| WebView crash blanks app | Renderer/error recovery planned. |
| Captive portal redirects | HTTPS allowlist blocks unknown hosts; show controlled error. |

## Allowed Domains

Provisional allowlist:

- `pos.example.com`
- `api.pos.example.com`
- `sub.pos.example.com`

Final domains must be supplied by project owner before production.

## Allowed Schemes

Allowed by default:

- `https`

Blocked by default:

- `http`
- `file`
- `content`
- `intent`
- `market`
- `javascript`
- `tel`
- `mailto`

## HTTPS Policy

- Production must use HTTPS only.
- `usesCleartextTraffic=false` is set.
- HTTP is blocked in `UrlPolicy`.
- Development exceptions require documentation and must not ship in release.

## Certificate Policy

- Do not bypass TLS certificate errors.
- Do not accept all certificates.
- Invalid certificate tests must show controlled failure.
- Certificate pinning is not part of MVP unless threat model changes.

## JavaScript Policy

- JavaScript is enabled provisionally because the internal web app likely requires it.
- JavaScript is not a trust boundary.
- Unsafe navigation from JavaScript must still pass URL policy.

## JavaScript Interface Policy

- `addJavascriptInterface` is prohibited by default.
- Any future bridge must have a dedicated ADR, method allowlist, tests, and threat model.

## Storage Policy

- DOM storage is enabled provisionally.
- File and content access are disabled.
- Cache/cookie clearing policy must be decided before production.

## Cookie Policy

- Cookies may be needed by the web system.
- Third-party cookies should remain disabled unless the web system proves need.
- Session lifetime and logout behavior are open questions.

## Logs Policy

Allowed:

- Admin event type.
- Kiosk state transitions.
- Blocked URL reason without full sensitive URL.
- Provisioning status.

Forbidden:

- PIN values.
- Cookies.
- Authorization headers.
- Session tokens.
- Full URLs with query strings.
- Personal data.

## Screenshot Policy

`FLAG_SECURE` is enabled by default. Disabling screenshots requires owner approval and documented reason.

## PIN Policy

- No real PIN in source code.
- No plain-text PIN in preferences.
- Current implementation supports PBKDF2 with Base64 hash and salt configuration.
- Default repository configuration is intentionally empty, so no PIN can verify out of the box.
- Production must provide hash/salt through an approved provisioning/configuration path.
- Android Keystore or remote validation remains recommended for production hardening.
- Gesture alone must never unlock kiosk.
- PIN attempts and lockouts should be rate-limited in final implementation.

## Android Keystore Recommendation

Use Android Keystore for protecting local verification material where possible. Keystore does not make a weak PIN strong by itself; combine with slow derivation, salt, rate limiting, and device policy controls.

## Credential Storage Risks

The APK can be decompiled. Any embedded credential, production PIN, API key, or shared secret must be considered exposed.

## Device Owner Restrictions

- Device Owner typically requires a factory-reset device.
- Existing personal accounts can block provisioning.
- Manufacturer customizations can affect policies.
- Real tablets must be tested before rollout.

## What The App Cannot Guarantee

- It cannot secure a rooted or physically compromised device absolutely.
- It cannot replace MDM/EMM fleet management.
- It cannot make an untrusted web app safe.
- It cannot prevent all actions before Device Owner provisioning.
- It cannot make immersive mode equivalent to Lock Task Mode.

## Environment Differences

| Environment | Policy |
| --- | --- |
| Development | Debug WebView allowed, diagnostics visible, provisioning may be partial. |
| Staging | Production-like hosts, no secrets, managed test devices preferred. |
| Production | Device Owner required, release build, WebView debugging off, no cleartext. |
