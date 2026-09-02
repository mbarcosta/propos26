# ADR-003: Use GMS as Email Gateway

Status: PROPOSED

## Decision

Preserve GMS as the email gateway and keep it process-agnostic.

## Rationale

GMS already polls mailboxes, normalizes messages, and moves processed messages. It should not decide business process meaning.

## Consequences

Process-specific rules should move out of GMS and into ADE-managed CIR configuration.

