# ADR 0001: Use Native Android WebView

## Status

Accepted.

## Context

The current system runs in Chrome, which allows tab switching, external browsing, and app escape. The kiosk needs controlled navigation and integration with Android device management APIs.

## Decision

Build a native Android app in Kotlin and render the internal web system through Android WebView.

## Consequences

- Native app can participate in Device Owner and Lock Task flows.
- URL navigation can be intercepted and controlled.
- WebView behavior must be hardened and tested.
- Web compatibility depends on Android System WebView version.

## Alternatives Considered

- Chrome Custom Tabs: rejected because it depends on Chrome behavior and does not give enough kiosk control.
- Keep Chrome: rejected because it is the problem being solved.
- Fully native rewrite: rejected because the business system already exists as web.
