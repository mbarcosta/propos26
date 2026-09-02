# Reference Process: Vinculacao de Orientacao

The reference process validates that the architecture can explain a real automation without hard-coding the platform to this process.

## Happy path

```text
Orientador
  -> Email
  -> GMS
  -> CIR
  -> Camunda
  -> Verificar dados
```

If data is missing:

```text
Camunda
  -> SEND_EMAIL
  -> Orientador
  -> resposta
  -> GMS
  -> CIR
  -> correlation
  -> Camunda
```

When data is complete:

```text
Camunda
  -> solicita confirmacao
  -> Estudante
  -> resposta
  -> GMS
  -> CIR
  -> Camunda
```

Then:

```text
Camunda
  -> Coordenador
  -> confirmacao
  -> CIR
  -> Camunda
```

Finally:

```text
Camunda
  -> REGISTER_ADVISORSHIP
  -> PPG Management Service
  -> Dissertation = IN_PROGRESS
```

## ADE configuration target

```text
BPMN element: Solicitar confirmacao do estudante
Capability: SEND_EMAIL
Recipient: ${student.email}
Template: advisor_confirmation_student
Expected Response Event: CONFIRMACAO_ESTUDANTE
Correlation Field: requestId
```

ADE should derive or publish:

- BPMN message definitions.
- CIR route definitions.
- GMS binding references.
- Capability bindings.
- Correlation definitions.
- Deployment manifest entries.

## Architectural validation

The process uses all core concepts:

- GMS as communication gateway.
- CIR as configurable inbound router.
- Camunda 7 as orchestration engine.
- Automation Services for validation, sending email, and registering advisorship.
- PPG Management Service for durable academic state.
- ADE as the design, configuration, deployment, and testing environment.

