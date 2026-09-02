# ADE MVP

Path:

```text
platform/automation-development-environment
```

Runtime:

```text
http://localhost:8070
```

## Areas

- Projects
- BPMN
- Automation
- Integrations
- Deployment
- Execution

## Capabilities

The ADE backend exposes:

```http
GET /api/capabilities
```

Initial capabilities:

- `SEND_EMAIL`
- `VALIDATE_ADVISORSHIP_REQUEST`
- `REGISTER_ADVISORSHIP`
- `VALIDATE_CPF`

## Deployment API

```http
POST /api/deployments
```

The deployment endpoint:

1. validates BPMN XML shape;
2. deploys BPMN to Camunda 7 using `/deployment/create`;
3. extracts the BPMN process id;
4. publishes a route set to CIR using `/api/cir/routes`;
5. returns deployment status.

## Genericity

The ADE UI works with generic concepts:

- project;
- BPMN XML;
- BPMN element;
- automation requirement;
- capability;
- integration;
- correlation;
- deployment.

The Vinculacao BPMN is loaded as a reference project, not as dedicated ADE screens.

