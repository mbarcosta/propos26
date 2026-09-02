# ADR-001: Use Camunda 7 as Process Engine

Status: PROPOSED

## Decision

Use Camunda 7 as the BPMN execution and orchestration engine.

## Rationale

The repository already contains Camunda 7 BPMN models, REST API usage, and External Task workers. Camunda 7 supports the required process-state responsibilities for E01.

## Consequences

ADE must target Camunda 7 BPMN extensions, External Task configuration, message correlation, and REST deployment APIs.

