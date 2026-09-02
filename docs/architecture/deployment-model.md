# Deployment Model

An automation is a versioned deployable unit composed of BPMN, services, configurations, and integrations.

## Automation Deployment Manifest

Conceptual example:

```yaml
automation:
  id: vinculacao-orientacao
  version: 1.0

process:
  file: vinculacao-orientacao.bpmn
  engine: camunda7

services:
  - capability: VALIDATE_REQUEST
    implementation: validate-request-service
    deployment: docker

  - capability: REGISTER_ADVISORSHIP
    implementation: ppg-management-service
    deployment: existing

integrations:
  - type: email
    provider: GMS

inboundEvents:
  - event: CONFIRMACAO_ESTUDANTE
    router: CIR
    correlationKey: requestId

environment:
  type: docker
```

## Deployment pipeline

```text
Validate BPMN
  -> Save automation version
  -> Resolve capabilities
  -> Build missing services
  -> Run tests
  -> Build Docker images
  -> Start or update containers
  -> Publish GMS configuration
  -> Publish CIR configuration
  -> Deploy BPMN to Camunda
  -> Validate endpoints and process start/correlation
  -> Produce deployment report
```

## Environments

Supported target environments:

- `DEVELOPMENT`
- `TEST`
- `PRODUCTION`

Environment-specific values must remain outside BPMN and source code:

- URLs.
- Ports.
- Credentials.
- Database connections.
- Email accounts.
- Camunda endpoints.

Use `.env`, `.env.example`, environment variables, and external configuration.

