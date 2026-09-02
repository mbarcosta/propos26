# E01 Report

## Initial state

The repository contained independent Spring Boot services instead of a root Maven multi-module project:

- GMS in `services/email-gateway-service`.
- CIR in `services/cir-service`.
- CPF validation service in `services/cpf-service`.
- Camunda External Task examples in `delivery-workers`.
- BPMN examples in `process_models`.

`docs/architecture/current-state.md` records the detailed discovery.

## Architecture created

E01 introduced the architectural foundation for propos26 as an assisted automation development ecosystem:

- ADE as the central design-time environment.
- Camunda 7 as the process engine.
- GMS as an email gateway.
- CIR as an inbound event router.
- Automation Capabilities and Capability Registry.
- Automation Services as reusable execution units.
- PPG Management Service as the domain-state owner.
- Automation Deployment as a composed deployment unit.

## Decisions

Initial ADRs were created under `docs/architecture/adr`:

- ADR-001 through ADR-008.

All are `PROPOSED` pending technical validation.

## Files created

- `docs/architecture/current-state.md`
- `docs/architecture/target-architecture.md`
- `docs/architecture/components.md`
- `docs/architecture/integration-contracts.md`
- `docs/architecture/embedded-bpmn-modeler.md`
- `docs/architecture/automation-development-environment.md`
- `docs/architecture/automation-capabilities.md`
- `docs/architecture/service-generation.md`
- `docs/architecture/deployment-model.md`
- `docs/architecture/reference-process.md`
- `docs/architecture/gap-analysis.md`
- `docs/architecture/adr/*`
- `platform/automation-development-environment/*`
- `platform/capability-registry/*`
- `domain-systems/ppg-management-service/README.md`
- `infrastructure/README.md`
- `infrastructure/.env.example`

## Files modified

No existing source files were modified in E01.

Validation regenerated tracked `.class` files under `services/email-gateway-service/target`. These are build outputs and should ideally not be versioned.

## GMS changes

GMS now resolves mail binding credentials from environment-backed Spring placeholders. The committed `data/bindings.json` no longer stores the concrete email username/password and uses `${GMS_MAIL_USERNAME}` and `${GMS_MAIL_PASSWORD}`.

## CIR changes

No CIR code changes were made. The architecture documents existing hard-coded rules and defines the target of ADE-managed routing/correlation configuration.

## BPMN editor PoC

A static PoC was added at:

```text
platform/automation-development-environment/poc/bpmn-editor/index.html
```

It loads `bpmn-js` from a CDN, renders a BPMN model, allows editing, and exports XML. Camunda 7 properties panel integration remains the next validation step.

## Gaps

See `docs/architecture/gap-analysis.md`.

Most important gaps:

- rotate and externalize committed email credential;
- create Docker Compose environment;
- replace CIR hard-coded process rules with configuration;
- remove or migrate GMS residual process-dispatch code;
- introduce a real Capability Registry service;
- define and validate deployment manifests.

## Technical debt

- Inconsistent Spring Boot versions.
- No root build orchestration.
- No OpenAPI specs.
- GMS URL hard-coded in CIR.
- No durable CIR processed-message store.
- Only one Dockerfile exists.

## Risks

- Embedded Camunda 7 properties editing still needs a real npm-based spike.
- Current credential exposure must be treated as an incident.
- Process-specific behavior may continue to accumulate in CIR if configuration is not introduced early in E02.

## Recommendations for E02

- Rotate the exposed email credential and remove secrets from committed JSON.
- Add Dockerfiles and a local `docker-compose.yml`.
- Externalize `GmsClient` base URL.
- Add CIR route configuration model for event -> message -> correlation mappings.
- Create a minimal Capability Registry Spring Boot service or lightweight static registry API.
- Replace the CDN modeler spike with pinned frontend dependencies and Camunda 7 properties support.

## Validation

Compilation:

- `services/email-gateway-service`: `mvn -q -DskipTests compile` passed.
- `services/cir-service`: `mvn -q -DskipTests compile` passed.
- `services/cpf-service`: `mvn -q -DskipTests compile` passed.
- `delivery-workers`: `mvn -q -DskipTests compile` passed.

Tests:

- `services/cir-service`: `mvn test` passed.
- `services/cpf-service`: `mvn test` passed.
- `services/email-gateway-service`: `mvn test` failed while resolving `surefire-junit-platform:3.1.2` from Maven Central because of a local PKIX certificate validation error.
- `delivery-workers`: `mvn test` failed while resolving `junit-platform-launcher:6.0.3` from Maven Central because of a local PKIX certificate validation error.

The failed test runs were retried with escalated network permissions and failed with the same PKIX certificate error.
