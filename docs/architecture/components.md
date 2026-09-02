# Components

## ADE

The Automation Development Environment is the primary user-facing environment. It coordinates modeling, requirement analysis, capability lookup, configuration, deployment, testing, and monitoring.

## Camunda 7

Camunda 7 is the process engine. It executes BPMN, manages process instance state, handles gateways, waits for messages, creates External Tasks, stores history, and exposes REST APIs.

Camunda must not become the database of academic domain state.

## GMS

GMS is the Email Gateway Service.

Responsibilities:

- Connect to email servers.
- Poll mailboxes.
- Normalize email messages.
- Apply mailbox/binding-level ingestion policies.
- Move processed messages.
- Expose normalized messages through an API.

GMS must not know process-specific meanings such as `CONFIRMACAO_ESTUDANTE`.

## CIR

CIR is the Camunda Inbound Router.

Responsibilities:

- Receive or fetch normalized external events.
- Classify events according to automation configuration.
- Start processes.
- Correlate messages with existing process instances.
- Send variables to Camunda.
- Track routing outcomes.

The CIR should progressively move from hard-coded rules to ADE-managed routing configuration.

## Automation Services

Automation Services execute capabilities used by BPMN processes.

Supported forms:

- `EXTERNAL_TASK_WORKER`
- `REST_SERVICE`
- `INTEGRATION_ADAPTER`
- `TRANSFORMER`
- `RULE_SERVICE`
- `AI_SERVICE`

## Capability Registry

The registry describes reusable capabilities available to automations. The ADE checks the registry before proposing new service generation.

## Domain Systems

Domain systems maintain durable business state. The initial domain system concept is `ppg-management-service`.

Responsibilities:

- Students.
- Professors.
- Advisorships.
- Dissertations.
- Programs.
- Courses.
- Research areas and lines.

