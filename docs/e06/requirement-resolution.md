# Requirement Resolution

The wizard resolves requirements from BPMN element type and existing ADE analysis.

## Supported Elements

- Start message event
- Service task
- Send task
- Receive task
- Intermediate message catch event
- Intermediate message throw event
- Exclusive gateway
- Message end event

## Mapping Rules

```text
Service Task
  CAPABILITY_BINDING
  VARIABLE_MAPPING
```

```text
Send Task
  OUTBOUND_COMMUNICATION
  CAPABILITY_BINDING
```

```text
Message Catch Event / Receive Task
  INBOUND_EVENT
  MESSAGE_DEFINITION
  CORRELATION_DEFINITION
```

```text
Exclusive Gateway
  CONDITION_VALIDATION
```

## Step Ordering

Steps follow XML/BPMN document order, which is deterministic and stable for the current ADE model. A final global validation step is appended.

Future work can replace this with graph traversal from start events.
