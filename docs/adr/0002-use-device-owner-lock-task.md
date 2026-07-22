# ADR 0002: Use Device Owner And Lock Task

## Status

Accepted.

## Context

Immersive mode hides system UI but does not secure the device. Screen pinning is user-facing and weaker than managed kiosk mode.

## Decision

Use Device Owner provisioning and DevicePolicyManager-managed Lock Task Mode for production kiosk behavior.

## Consequences

- Production tablets need controlled provisioning, usually after factory reset.
- ADB can support development provisioning.
- MDM/EMM remains a future fleet-management option.
- The app must clearly show non-provisioned state instead of pretending to be secure.

## Alternatives Considered

- Immersive mode only: rejected as false security.
- Screen pinning only: rejected as weaker and user-controlled.
- MDM-only app: deferred until fleet requirements are known.
