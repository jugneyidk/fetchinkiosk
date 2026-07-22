# ADR 0006: No JavaScript Bridge By Default

## Status

Accepted.

## Context

`addJavascriptInterface` can expose native app APIs to web content. This increases attack surface, especially if pages or origins are ever compromised.

## Decision

Do not add a JavaScript bridge in the initial design.

## Consequences

- Web content cannot call native methods by default.
- Feature requests needing native integration need separate threat modeling.
- Any future bridge must define exact allowed methods, origins, tests, and failure behavior.

## Alternatives Considered

- Add generic bridge for convenience: rejected as unsafe.
- Add narrow bridge now: rejected because no requirement exists.
