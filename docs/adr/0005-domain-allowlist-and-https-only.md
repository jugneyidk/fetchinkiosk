# ADR 0005: Domain Allowlist And HTTPS Only

## Status

Accepted.

## Context

The app must prevent users and web content from navigating outside approved internal systems. Naive string matching can allow suffix attacks.

## Decision

Use an explicit HTTPS-only host allowlist. A URL is allowed only when its host equals an allowed host or is a true subdomain separated by a dot.

## Consequences

- `https://pos.example.com` can be allowed.
- `https://sub.pos.example.com` can be allowed through host boundary logic.
- `https://pos.example.com.attacker.com` is blocked.
- HTTP and unsafe schemes are blocked.
- Final production hosts remain owner decisions.

## Alternatives Considered

- `url.contains(host)`: rejected as vulnerable.
- Plain `endsWith(host)` without dot boundary: rejected as vulnerable.
- Regex-only policy: rejected as less readable for MVP.
