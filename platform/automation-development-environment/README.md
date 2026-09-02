# Automation Development Environment

MVP web application for E02.

## Features

- Create/open an Automation Project in the browser.
- Load/edit/save/export BPMN.
- Analyze BPMN elements and list Automation Requirements.
- Bind BPMN elements to capabilities.
- Configure inbound/correlation definitions.
- Validate project configuration.
- Deploy BPMN to Camunda 7 through the ADE backend.
- Show deployment state.

## Run locally

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8070
```

## Docker

The root `docker-compose.yml` builds this application as part of the propos26 Automation Platform.

## Configuration

```text
CAMUNDA_BASE_URL=http://localhost:8080/engine-rest
CIR_BASE_URL=http://localhost:8082
GMS_BASE_URL=http://localhost:8081
```

The UI is generic. The reference automation data is an example project loaded into ADE concepts, not process-specific ADE code.

