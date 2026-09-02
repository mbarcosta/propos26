# ADR-002: Separate Process State and Domain State

Status: PROPOSED

## Decision

Camunda stores process state. Domain systems store durable academic/business state.

## Rationale

Process variables and history are not a replacement for systems of record such as students, professors, advisorships, and dissertations.

## Consequences

Introduce `ppg-management-service` as the conceptual domain system for academic data.

