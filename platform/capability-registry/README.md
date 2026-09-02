# Capability Registry

Initial skeleton for the Automation Capability Registry.

The registry will expose available capabilities to ADE so it can bind BPMN requirements to existing services before generating new components.

## E01 contract sketch

```http
GET /api/capabilities
GET /api/capabilities/{id}
POST /api/capabilities
PUT /api/capabilities/{id}
```

## Example data

See `examples/capabilities.json`.

