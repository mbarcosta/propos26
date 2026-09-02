# ADR-006: Embed BPMN Modeling in ADE

Status: PROPOSED

## Decision

ADE should embed a web BPMN modeler compatible with Camunda 7.

## Rationale

Switching repeatedly between external modelers and platform configuration would make automation construction too technical for the target user.

## Consequences

Use `camunda-bpmn-js` when possible, or `bpmn-js` with Camunda moddle/properties panel support.

