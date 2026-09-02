# E02 Overview

E02 turns the E01 architecture into a first operational automation platform.

Implemented units:

- propos26 Automation Platform:
  - ADE
  - Camunda 7
  - GMS
  - CIR
  - Automation workers
  - CPF service
- PPG Management Service:
  - independent REST/UI domain system
  - independent Docker Compose

The two units can be started separately. Integration happens only by REST through `PPG_MANAGEMENT_BASE_URL`.

## Operational shape

```text
propos26 Automation Platform
  ADE -> Camunda 7
  ADE -> CIR routes
  GMS -> CIR -> Camunda 7
  Camunda 7 -> automation workers
  automation workers -> PPG Management REST

PPG Management Service
  REST API + UI + persisted JSON state
```

## E02 scope choices

The PPG MVP uses a persisted JSON file instead of JPA/H2. This keeps the service independent and operational with Docker volume persistence while avoiding new dependency risk in this sprint. It is documented as E03 hardening debt.

