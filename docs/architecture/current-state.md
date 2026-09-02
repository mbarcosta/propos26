# Current State

This document records the state found in `propos26` before Sprint E01 architecture changes.

## Repository shape

The repository is not currently a single Maven multi-module project. It is a collection of independent projects and documentation:

- `services/email-gateway-service`: Spring Boot application. Current GMS.
- `services/cir-service`: Spring Boot application. Current CIR.
- `services/cpf-service`: Spring Boot REST service and the only service with a Dockerfile.
- `delivery-workers`: Spring Boot application using the Camunda 7 External Task Client.
- `process_models`: BPMN files and Camunda 7 notes.
- `docs`: general project documents.
- `exemploJava`: standalone Java example, currently untracked.
- `spec`: sprint/specification material, currently untracked.

There is no root `pom.xml`, no root `docker-compose.yml`, and no shared contracts module.

## Maven projects

| Path | Artifact | Spring Boot | Java | Notes |
| --- | --- | --- | --- | --- |
| `services/email-gateway-service` | `br.ifes:email-gateway-service` | 3.2.5 | 17 | GMS, REST + Jakarta Mail. |
| `services/cir-service` | `br.ifes:cir-service` | 3.5.13 | 17 | CIR, REST client to GMS and Camunda. Duplicate `spring-boot-starter-web` dependency. |
| `services/cpf-service` | `br.ifes:cpf-service` | 3.2.5 | 17 | Simple CPF validation REST service. |
| `delivery-workers` | `br.edu.ifes:delivery-workers` | 4.0.6 | 17 | Camunda External Task workers for delivery quotation examples. |

## Spring Boot applications

- `EmailGatewayServiceApplication`
- `CirServiceApplication`
- `CpfServiceApplication`
- `DeliveryWorkersApplication`

## Docker

- Existing Dockerfile: `services/cpf-service/Dockerfile`.
- No Dockerfile was found for GMS, CIR, delivery workers, ADE, or a domain system.
- No Docker Compose file was found.
- Camunda 7 is documented as a manual Docker command using `camunda/camunda-bpm-platform:run-latest`.

## GMS: Email Gateway Service

Location: `services/email-gateway-service`.

Current responsibility:

- Load mail bindings from `data/bindings.json` through `app.bindings.file`.
- Connect to an IMAP server.
- Poll messages for a configured binding.
- Normalize email messages into `EmailMessage`.
- Return a `PollResult`.
- Move messages to the configured `Processed` folder after processing confirmation.

REST API:

- `POST /api/bindings/{bindingId}/poll`
- `POST /api/bindings/{bindingId}/messages/processed`

Important models:

- `MailBinding`
- `MailServerConfig`
- `MailFolderConfig`
- `PollingPolicy`
- `IngestionPolicy`
- `EmailMessage`
- `PollResult`
- `PollItemResult`
- `OperationResult`
- `MarkAsProcessedRequest`

Observed coupling and risks:

- `data/bindings.json` previously contained real email account configuration and a credential. It now uses environment placeholders, but the exposed credential must still be considered compromised and rotated.
- `src/main/resources/config/email-rules.json`, `RuleLoader`, `RuleMatcher`, `EmailRule`, `EmailEvent`, and `ProcessDispatcher` indicate an older process-dispatch responsibility inside GMS. The active polling controller does not need GMS to interpret process semantics.
- GMS is already close to the desired gateway role, but residual rule/dispatcher code should be retired or moved out in a later sprint.

## CIR: Camunda Inbound Router

Location: `services/cir-service`.

Current responsibility:

- Expose a REST endpoint to execute routing for a binding.
- Poll GMS.
- Classify GMS messages.
- Start Camunda process instances through `/engine-rest/message`.
- Correlate reply messages through `/engine-rest/message`.
- Ask GMS to mark successfully routed messages as processed.
- Maintain an in-memory local processed-message store.

REST API:

- `POST /api/cir/execute?bindingId={bindingId}`

Main classes:

- `CirService`
- `GmsClient`
- `CamundaClient`
- `MessageEventClassifier`
- `ProcessedMessageStore`
- `ClassifiedMessage`
- `CirExecutionResult`

Current Camunda operations:

- Start message: `CamundaClient.sendStartMessage(messageName, businessKey, variables)`
- Reply correlation: `CamundaClient.sendReplyMessage(messageName, correlationId, variables)`
- Compatibility send: `CamundaClient.sendMessage(messageName, variables)`
- External task helper methods exist but are not central to the current CIR flow.

Observed coupling and risks:

- `GmsClient` hard-codes `http://localhost:8081`.
- `MessageEventClassifier` hard-codes process-specific subject rules: `vinculacao`, `defesa`, `matricula`.
- Message names are hard-coded: `VINCULACAO_START`, `DEFESA_START`, `MATRICULA_START`, `EMAIL_REPLY`.
- `generateCorrelationId` in `CirService` embeds process prefixes `VINC-` and `DEF-`.
- `VinculacaoProcessService` and `VinculacaoHandler` are process-specific classes still present in CIR.
- There is a likely bug in `CirService`: when a new `correlationId` is generated for a START message, the generated value is added to variables but `sendStartMessage` is called with `message.getCorrelationId()`, which may still be null.

## Camunda 7 integration

Camunda 7 is used through REST endpoints at `http://localhost:8080/engine-rest`.

Current integration points:

- CIR uses `/message` for start and correlation.
- `delivery-workers` uses `camunda-external-task-client` version `7.23.0`.
- BPMN files were exported by Camunda Modeler and target Camunda Platform 7.24.0.

There is no embedded Camunda engine in the repository and no Camunda deployment automation in code.

## External task workers

Location: `delivery-workers`.

Current workers:

- `MotoWorker`: subscribes to topic `cotacao-moto`, reads `distanciaKm`, returns `valorCotacao`.
- `CarroWorker`: subscribes to topic `cotacao-carro`, reads `distanciaKm`, returns `valorCotacao`.

These workers demonstrate the Camunda External Task pattern but are tied to the delivery quotation example.

## REST automation services

Location: `services/cpf-service`.

Endpoint:

- `GET /cpf/{cpf}`

Response shape:

```json
{
  "cpf": "...",
  "valid": true
}
```

This service is a candidate example of an Automation Capability.

## BPMN models

Existing BPMN files:

- `process_models/teste/teste_v1.bpmn`: message start event `VINCULACAO_RECEBIDA`, external service task topic `vinculacao`.
- `process_models/exemplos/deliveryWorkers.bpmn`: delivery quotation example with topics `cotacao-moto` and `cotacao-carro`.
- `process_models/base/matricula_v1.bpmn`: Camunda 7 BPMN model for enrollment.

The current BPMN assets are examples and do not yet form an ADE-managed Automation Project.

## Configuration mechanisms

- GMS:
  - `server.port=8081`
  - `app.bindings.file=data/bindings.json`
  - JSON binding file with mail connection, folders, polling policy, ingestion policy.
- CIR:
  - `server.port=8082`
  - `camunda.base-url=http://localhost:8080/engine-rest`
  - GMS URL is hard-coded in Java.
- Delivery workers:
  - `server.port=8091`
  - `camunda.bpm.client.base-url=http://localhost:8080/engine-rest`
  - `camunda.bpm.client.worker-id=delivery-workers`
- CPF service:
  - no custom runtime configuration found.

## Current message correlation

Current generic mechanism:

- Replies are detected when `CORRELATION-ID: ...` appears in subject/body or when an id appears in brackets in the subject.
- Replies are sent to Camunda as message `EMAIL_REPLY`.
- Correlation key name is fixed as `correlationId`.

Missing:

- Configurable mapping from external event to BPMN message.
- Configurable correlation variable name.
- Configurable process definition per automation.
- Durable processed-message store.

## Current dependencies

Runtime dependency flow:

```text
GMS -> mail server
CIR -> GMS
CIR -> Camunda 7 REST
delivery-workers -> Camunda 7 REST
BPMN -> External Task topics
CPF service -> standalone REST
```

No module-level compile-time dependency exists among the services.

## Current process-specific coupling

Process-specific content exists in:

- `services/cir-service/.../MessageEventClassifier.java`
- `services/cir-service/.../CirService.java`
- `services/cir-service/.../VinculacaoProcessService.java`
- `services/cir-service/.../VinculacaoHandler.java`
- `services/email-gateway-service/src/main/resources/config/email-rules.json`
- `services/email-gateway-service/.../ProcessDispatcher.java`
- `delivery-workers` topics and business rules.
- BPMN message names and External Task topics.

This is acceptable for the current prototype but conflicts with the target where ADE configures automation behavior.
