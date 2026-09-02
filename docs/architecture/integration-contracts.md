# Integration Contracts

## GMS to CIR

Polling:

```http
POST /api/bindings/{bindingId}/poll
```

Conceptual response:

```json
{
  "bindingId": "ppcomp-main",
  "totalRead": 1,
  "messages": [
    {
      "messageId": "...",
      "channel": "EMAIL",
      "from": "...",
      "to": ["..."],
      "cc": ["..."],
      "subject": "...",
      "body": "...",
      "receivedAt": "...",
      "bindingId": "ppcomp-main",
      "inReplyTo": "...",
      "references": ["..."],
      "hasAttachments": false
    }
  ]
}
```

Current implementation does not include `channel` or `bindingId` inside each `EmailMessage`; those fields should be added only through a compatible DTO evolution.

Processed acknowledgement:

```http
POST /api/bindings/{bindingId}/messages/processed
Content-Type: application/json

{
  "messageId": "..."
}
```

## CIR to Camunda

Start process by BPMN message:

```json
{
  "messageName": "VINCULACAO_START",
  "businessKey": "VINC-...",
  "processVariables": {
    "messageId": { "value": "...", "type": "String" },
    "from": { "value": "...", "type": "String" },
    "subject": { "value": "...", "type": "String" },
    "body": { "value": "...", "type": "String" },
    "correlationId": { "value": "VINC-...", "type": "String" }
  }
}
```

Correlate message:

```json
{
  "messageName": "EMAIL_REPLY",
  "correlationKeys": {
    "correlationId": { "value": "VINC-...", "type": "String" }
  },
  "processVariables": {
    "body": { "value": "...", "type": "String" }
  }
}
```

Target evolution:

```text
External Event -> BPMN Message -> Correlation Variable -> Camunda operation
```

This mapping must become ADE-managed configuration.

## Camunda to Automation Services

Preferred integration for decoupled work:

```text
BPMN Service Task
  camunda:type="external"
  camunda:topic="CAPABILITY_TOPIC"
```

Worker completion returns process variables through the Camunda External Task API.

REST services remain acceptable when synchronous request/response is architecturally appropriate.

## Automation Services to Domain Systems

Automation Services must use public APIs of domain systems. They must not access another component's database directly.

