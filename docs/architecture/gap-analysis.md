# Gap Analysis

## Critical

- A real email credential was present in repository history/configuration. Rotate it and keep credentials in external secrets/configuration.
- No Docker Compose environment exists for the ecosystem.
- No deployment manifest exists for automations.
- CIR still has process-specific hard-coded routing rules.
- GMS still contains residual process-rule/dispatcher classes and sample process rules.

## Required

- Introduce ADE project model and sidecar metadata.
- Introduce Capability Registry implementation.
- Externalize GMS URL in CIR.
- Define durable CIR processed-message/routing state.
- Add explicit DTO/contract ownership for GMS -> CIR and CIR -> Camunda.
- Define configurable mapping from external events to BPMN messages and correlation variables.
- Add Dockerfiles for GMS, CIR, ADE, workers, and generated services.
- Add `.env.example` and remove environment-specific values from committed config.

## Desirable

- Create a root build or workspace orchestration script.
- Align Spring Boot versions across services.
- Add OpenAPI specs for GMS, CIR, Capability Registry, and domain services.
- Add integration tests with Camunda 7 test container or local compose profile.
- Add health checks and structured logs.

## Future

- Full ADE UI.
- Assisted service generation.
- Automated build -> containerize -> deploy pipeline.
- Promotion across DEVELOPMENT, TEST, and PRODUCTION.
- Monitoring console for instances, messages, variables, incidents, and workers.
