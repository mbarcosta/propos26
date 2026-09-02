# Target Architecture

`propos26` must evolve from a set of Camunda-integrated services into an assisted process-automation development environment.

Guiding principle:

```text
Model first; configure when possible; generate code when necessary; program manually only as a last resort.
```

## Architecture cycle

```text
MODELAR
  -> ANALISAR NECESSIDADES DE AUTOMACAO
  -> LOCALIZAR CAPACIDADES EXISTENTES
  -> CRIAR/CONFIGURAR CAPACIDADES AUSENTES
  -> VINCULAR CAPACIDADES AO BPMN
  -> CONFIGURAR EVENTOS, MENSAGENS E CORRELACOES
  -> IMPLANTAR
  -> EXECUTAR
  -> TESTAR
  -> MONITORAR
```

Every component exists to support one or more steps of this cycle.

## Components

```text
                       +--------------------------------------+
                       | Automation Development Environment   |
                       | ADE                                  |
                       |--------------------------------------|
                       | Embedded BPMN Modeler                |
                       | Requirement Analyzer                 |
                       | Capability Registry                  |
                       | Service Generator                    |
                       | Integration Configurator             |
                       | Deployment Manager                   |
                       | Testing / Execution Console          |
                       +------------------+-------------------+
                                          |
            +-----------------------------+----------------------------+
            |                             |                            |
            v                             v                            v
    +---------------+              +---------------+           +---------------+
    | GMS           |              | CIR           |           | Camunda 7     |
    | Email Gateway |------------->| Event Router  |---------->| Process Engine|
    +---------------+              +---------------+           +-------+-------+
                                                                         |
                                      +----------------------------------+----------------+
                                      |                                                   |
                                      v                                                   v
                           +-----------------------+                         +----------------------+
                           | Automation Services   |                         | Domain Systems       |
                           | Workers, REST, AI     |                         | PPG Management       |
                           +-----------------------+                         +----------------------+
```

## Logical organization

Target organization, to be adopted incrementally:

```text
propos26/
├── platform/
│   ├── automation-development-environment/
│   └── capability-registry/
├── process-engine/
│   └── camunda7/
├── integration/
│   ├── email-gateway-service/
│   └── camunda-inbound-router/
├── automation-services/
│   ├── workers/
│   └── services/
├── domain-systems/
│   └── ppg-management-service/
├── processes/
├── infrastructure/
└── docs/
```

Existing code should not be moved only for visual cleanliness. Movement should happen when contracts, deployment, or ownership boundaries require it.

## Automation Project and Deployment

An `AutomationProject` is the design-time unit. It contains BPMN, automation requirements, capability bindings, integration definitions, tests, and environment-independent deployment intent.

An `AutomationDeployment` is the runtime operational unit. It contains a deployable version of the BPMN, service versions, GMS/CIR configuration, environment configuration, and validation evidence.

```text
Automation Project
        -> build
Automation Deployment
        -> BPMN + Services + CIR/GMS configuration + environment bindings
```

## Deployment pipeline

```text
BPMN + Automation Configuration
  -> Validate Automation
  -> Resolve Capabilities
  -> Build Required Services
  -> Run Tests
  -> Build Docker Images
  -> Deploy/Start Containers
  -> Configure GMS and CIR
  -> Deploy BPMN
  -> Validate Deployment
  -> Automation READY
```

Reusable deployed capabilities must not be rebuilt or redeployed unnecessarily.

## State ownership

- Camunda 7 owns process state: tokens, waiting events, process variables, incidents, history.
- Domain systems own durable business state: students, professors, advisorships, dissertations, programs.
- GMS owns communication ingestion state.
- CIR owns inbound routing/correlation state and route execution status.
- ADE owns automation design-time metadata.

