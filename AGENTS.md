# Fetchin Kiosk Agent Rules

This file is the highest project authority for future agents working on Fetchin Kiosk.

## Purpose

Fetchin Kiosk is a native Android application that restricts a tablet to one authorized internal web system through a controlled WebView, Device Owner provisioning, DevicePolicyManager, and Lock Task Mode.

## Authority Order

1. `AGENTS.md`
2. `docs/ARCHITECTURE.md`
3. `docs/SECURITY_MODEL.md`
4. `docs/IMPLEMENTATION_PLAN.md`
5. `docs/ACCEPTANCE_CRITERIA.md`
6. Existing code and tests

When documents conflict, follow the higher authority and update the lower document in the same change. If code conflicts with security docs, do not weaken security silently; either fix code or document a justified deviation.

## Files To Read Before Coding

- `AGENTS.md`
- `docs/PROJECT_STATUS.md`
- `docs/ARCHITECTURE.md`
- `docs/SECURITY_MODEL.md`
- `docs/IMPLEMENTATION_PLAN.md`
- `docs/ACCEPTANCE_CRITERIA.md`
- `docs/OPEN_QUESTIONS.md`
- Relevant ADRs in `docs/adr/`

## Approved Stack

- Native Android.
- Kotlin.
- Gradle Kotlin DSL.
- AndroidX.
- XML Views and ViewBinding.
- Single `app` module for MVP.
- WebView, not Chrome Custom Tabs.
- Minimum SDK API 26 unless real device requirements force change.
- Compile/target SDK must match installed stable SDK and be documented.

## Architecture Rules

- Keep one `app` module until a real modularity pressure exists.
- Keep `MainActivity` as coordinator only.
- Put URL policy in `web`.
- Put Device Owner and admin receiver code in `admin` or `kiosk`.
- Put central configuration in `config`.
- Put UI state in `ui`.
- Put security logging and security helpers in `security`.
- Do not duplicate security rules across classes.
- Do not hide provisioning failure from the user or logs.

## Package Conventions

- Root package is provisional: `com.fetchin.kiosk`.
- Packages use lowercase names.
- Class names describe responsibility, not implementation detail.
- Tests mirror production package paths.

## Code Comment Rule

Project owner rule: do not add inline or block comments inside code files. If a code explanation is truly necessary, place a short file-level header comment only. Prefer expressive names and documentation files.

## Security Rules

- Never introduce secrets, API keys, real passwords, production PINs, tokens, or certificates into the repository.
- Never store admin PIN as plain text.
- Never bypass URL allowlist to “make it work.”
- Never enable cleartext traffic for production.
- Never enable `addJavascriptInterface` without a documented threat model and exact allowed interface.
- Never allow `file://`, `content://`, `intent://`, `market://`, `javascript:`, `tel:`, or `mailto:` unless a requirement and mitigation are documented.
- Never treat immersive mode or screen pinning as secure kiosk mode.
- Never claim real kiosk security without Device Owner or MDM/EMM.

## WebView Rules

- WebView may use JavaScript and DOM storage only because the internal system needs them.
- File and content access stay disabled.
- Multiple windows stay disabled.
- External schemes are blocked by default.
- Downloads are blocked until explicitly designed.
- WebView debugging is allowed only in debug builds.
- URL validation must be covered by unit tests.

## Device Owner Rules

- Device Admin is not the same as Device Owner.
- Lock Task Mode must be allowlisted by DevicePolicyManager for production behavior.
- Do not run Device Owner ADB commands on a real device unless the project owner explicitly asks.
- Document provisioning commands only.
- Development fallback must be visibly marked as weaker than production.

## Gradle Rules

- Use version catalog in `gradle/libs.versions.toml`.
- Do not hardcode dependency versions in module build files.
- Add dependencies only when necessary and document why.
- Prefer AndroidX and platform APIs over new frameworks.
- Do not add Hilt, Room, Retrofit, Firebase, or Navigation unless an approved task requires them.

## Change Rules

- Make the smallest correct change.
- Keep docs and code synchronized in the same change.
- Preserve existing work; do not delete or overwrite without understanding purpose.
- No destructive commands.
- No mass rewrites unless explicitly approved.
- Mark provisional decisions in `docs/OPEN_QUESTIONS.md`.
- Record significant deviations in `docs/adr/` or relevant docs.

## Required Validation

Run or document why unable:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```

Also check for obvious secrets before handoff.

## Definition Of Done

- Code compiles or failure is documented with exact command and error.
- Relevant unit tests pass or failure is documented.
- Lint passes or failure is documented.
- Security model remains intact.
- Documentation reflects actual structure.
- `docs/PROJECT_STATUS.md` is updated with real results.
- `docs/HANDOFF_ANTIGRAVITY.md` is updated when handing work to another agent.
