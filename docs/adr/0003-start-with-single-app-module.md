# ADR 0003: Start With Single App Module

## Status

Accepted.

## Context

The MVP has one Activity, one WebView, one URL policy, one kiosk controller, and small supporting classes.

## Decision

Use one Android `app` module and organize code by package responsibility.

## Consequences

- Faster setup and simpler Gradle configuration.
- No artificial module boundaries.
- Package boundaries must be respected by review.
- Modularization can be introduced later when real pressure appears.

## Alternatives Considered

- Multi-module clean architecture: rejected as premature.
- Single Activity with all logic inline: rejected because security rules must be testable and isolated.
