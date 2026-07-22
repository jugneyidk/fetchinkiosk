# Open Questions

Resolve these before the listed phase limit.

| Question | Impact | Provisional Option | Decision Required | Phase Limit |
| --- | --- | --- | --- | --- |
| Production URL | App cannot ship safely without real start URL | `https://pos.example.com` | Exact URL | Before Phase 2 completion |
| Allowed domains | URL policy depends on exact hosts | `pos.example.com`, `api.pos.example.com`, `sub.pos.example.com` | Full domain allowlist | Before Phase 2 completion |
| Final app name | User-facing install identity | Fetchin Kiosk | Final label | Before release |
| Package name | ADB provisioning and Play/MDM identity | `com.fetchin.kiosk` | Final package/applicationId | Before production provisioning |
| Orientation | UX and tablet mounting | Landscape | Landscape, portrait, sensor, or config | Before Phase 1 completion |
| Camera need | Requires permissions/security review | Not needed | Yes/no and use case | Before adding permission |
| Printing need | Adds peripherals/intents/network | Not needed | Printer model/protocol | Before Version 1 |
| File upload need | WebView file chooser risk | Blocked | Yes/no and allowed MIME types | Before Phase 2 completion |
| Downloads need | Storage/security risk | Blocked | Yes/no and allowed file types | Before Phase 2 completion |
| Telephone links | External scheme risk | Blocked | Allow or block | Before Phase 2 completion |
| Mail links | External scheme risk | Blocked | Allow or block | Before Phase 2 completion |
| Authentication model | Session/cookie/logout behavior | Web app handles auth | Required session policy | Before Phase 5 |
| Session timeout | Privacy and operational behavior | Follow web app | Timeout and idle policy | Before Phase 5 |
| Update policy | Fleet reliability | Manual ADB in development | MDM/EMM/manual process | Before production |
| PIN strategy | Admin escape security | Interface only | Keystore local, derived hash, or remote validation | Before Phase 4 completion |
| Target tablets | OEM policy differences | Unknown Android tablet | Manufacturer/model/Android version | Before Phase 6 validation |
| MDM future | Config/update strategy | Future roadmap | Vendor or no MDM | Before fleet rollout |
| Android SDK version | Build reproducibility | compile/target API 36 | Confirm production SDK baseline | Before release |
