# ADR 0004: Use XML Views

## Status

Accepted.

## Context

The requested stack prefers XML Views. The UI is simple: WebView plus status/error/maintenance states.

## Decision

Use XML Views with ViewBinding.

## Consequences

- Lower framework complexity for MVP.
- Familiar Android interoperability with WebView.
- No Compose dependency or compiler setup needed.
- Future Compose migration would require a new ADR.

## Alternatives Considered

- Jetpack Compose: rejected because no current UI complexity justifies it.
- Raw `findViewById`: rejected because ViewBinding gives safer references with little cost.
