# Automation Capabilities

An Automation Capability describes something the ecosystem can do for a process.

Example:

```json
{
  "id": "SEND_EMAIL",
  "name": "Send Email",
  "type": "INTEGRATION",
  "provider": "GMS",
  "interface": "REST",
  "status": "AVAILABLE"
}
```

Example:

```json
{
  "id": "VALIDATE_CPF",
  "name": "Validate CPF",
  "type": "BUSINESS_SERVICE",
  "provider": "cpf-service",
  "interface": "REST",
  "status": "AVAILABLE"
}
```

## Resolution flow

```text
BPMN task/event
  -> Automation Requirement
  -> Capability lookup
  -> existing capability found?
      yes -> configure binding
      no  -> generate or implement service
```

## Initial registry fields

- `id`
- `name`
- `description`
- `type`
- `provider`
- `interface`
- `status`
- `version`
- `inputSchema`
- `outputSchema`
- `runtime`
- `endpoint`
- `externalTaskTopic`
- `deploymentMode`

## Current candidates

- `VALIDATE_CPF`: backed by `services/cpf-service`.
- `CALCULATE_DELIVERY_QUOTE_MOTO`: backed by `delivery-workers` topic `cotacao-moto`.
- `CALCULATE_DELIVERY_QUOTE_CARRO`: backed by `delivery-workers` topic `cotacao-carro`.
- `READ_EMAIL`: backed by GMS.
- `ROUTE_INBOUND_EVENT`: backed by CIR.

