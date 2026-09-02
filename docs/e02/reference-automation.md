# Reference Automation

Reference process:

```text
Vinculacao de Orientacao
```

The ADE includes a reference BPMN XML in the browser application. It contains:

- message start event `VINCULACAO_SOLICITADA`;
- service task topic `VALIDATE_ADVISORSHIP_REQUEST`;
- send tasks for outbound communication;
- message catch events for replies/confirmations;
- service task topic `REGISTER_ADVISORSHIP`;
- end event.

## Capabilities

| BPMN element | Capability |
| --- | --- |
| Verificar dados | `VALIDATE_ADVISORSHIP_REQUEST` |
| Registrar orientacao | `REGISTER_ADVISORSHIP` |
| Send tasks | `SEND_EMAIL` |

Message throw events are detected as outbound communication requirements in the ADE. For execution through a Camunda External Task worker, model the outbound notification as a BPMN Send Task or Service Task and bind it to `SEND_EMAIL`.

## Domain integration

The `REGISTER_ADVISORSHIP` worker posts to:

```http
POST {PPG_MANAGEMENT_BASE_URL}/api/advisorships
```

Payload fields are read from Camunda process variables:

- `studentId`
- `advisorId`
- `title`
- `researchArea`
- `startDate`
