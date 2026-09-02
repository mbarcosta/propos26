# ADR-004: Use CIR as Inbound Event Router

Status: PROPOSED

## Decision

Preserve CIR as the router between external normalized events and Camunda.

## Rationale

CIR already integrates GMS and Camunda and has start/correlation responsibilities.

## Consequences

CIR must evolve from hard-coded classifiers to configurable routing definitions.

