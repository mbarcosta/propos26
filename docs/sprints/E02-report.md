# E02 Report

## Initial state

E02 started from the E01 architecture and a repository with independent Spring Boot services for GMS, CIR, CPF, and delivery workers. No ADE backend, root Docker Compose, or independent PPG Management implementation existed.

## Decisions

- ADE is implemented as a Spring Boot web app with static frontend and backend deployment proxy.
- PPG Management is independent and communicates only over REST.
- PPG Management uses JSON-file persistence in E02, with JPA/relational persistence deferred to E03.
- CIR route configuration is JSON-backed and exposed through REST.
- The automation worker integrates Camunda to PPG Management through `PPG_MANAGEMENT_BASE_URL`.

## Architecture implemented

- Root propos26 Automation Platform Docker Compose.
- Independent PPG Management Docker Compose.
- ADE MVP for project configuration, BPMN editing, requirement analysis, capability binding, integration configuration, validation, and deployment.
- CIR configurable routing API.
- Automation workers for `VALIDATE_ADVISORSHIP_REQUEST` and `REGISTER_ADVISORSHIP`.

## ADE functionality

- Browser UI at `http://localhost:8070`.
- Areas: Projects, BPMN, Automation, Integrations, Deployment, Execution.
- Embedded `bpmn-js` modeler.
- BPMN analysis for service tasks, send tasks, receive/message catch elements.
- Capability list API.
- Deployment to Camunda 7.
- CIR route publication during deployment.

## PPG Management functionality

- REST API for students, professors, advisorships, and health.
- Minimal UI at `http://localhost:8090`.
- Docker Compose independent from propos26.
- Persisted state through `PPG_DATA_FILE`.

## GMS changes

- Added `GET /api/health`.
- Added Dockerfile.
- Existing credential placeholder support remains in place.

## CIR changes

- Added `gms.base-url` external configuration.
- Added configurable route repository.
- Added `GET/POST /api/cir/routes`.
- Added `GET /api/cir/health`.
- Classifier uses route configuration before old fallback rules.
- Fixed generated start correlation id being omitted from Camunda start call.

## Docker components

Root `docker-compose.yml` includes:

- Camunda 7
- ADE
- GMS
- CIR
- CPF service
- Automation workers

PPG Management has its own `docker-compose.yml`.

## APIs created

See `docs/e02/apis.md`.

## Problems found

- Maven test dependency resolution for some modules previously failed due local PKIX/certificate issues.
- Full email-driven E2E requires a real mailbox and Camunda runtime; this environment was not fully exercised with live Docker containers in this turn.
- PPG persistence is operational but not relational/JPA yet.

## Limitations

- ADE deployment validation is still shallow.
- ADE does not yet persist projects server-side.
- GMS configuration publishing remains file/environment based.
- CIR route publication currently replaces the route set.
- Instance monitoring is not implemented.
- Camunda 7 properties panel support in ADE remains basic.

## Validation results

Compilation passed with:

- ADE: `mvn -q -DskipTests compile`
- PPG Management: `mvn -q -DskipTests compile`
- GMS: `mvn -q -DskipTests compile`
- CIR: `mvn -q -DskipTests compile`
- Automation workers: `mvn -q -DskipTests compile`

## E2E status

The code path for the E2E is present:

```text
ADE deploys BPMN
ADE publishes CIR routes
CIR can route GMS messages to Camunda
Camunda creates External Tasks
REGISTER_ADVISORSHIP worker calls PPG Management REST
PPG Management persists and displays Advisorship
```

The live Docker/mail/Camunda run still needs to be executed in the target workstation environment with valid mailbox credentials.

## Recommendations for E03

- Replace PPG JSON persistence with JPA and relational database.
- Add OpenAPI specs generated from code.
- Add server-side ADE project persistence.
- Add robust deployment manifest support.
- Add GMS configuration API.
- Add non-destructive CIR route merge/update semantics.
- Add Camunda instance monitoring in ADE.
- Add automated E2E tests against Docker Compose.

