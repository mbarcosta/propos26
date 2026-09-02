# Service Generation

ADE should eventually generate skeletons for missing Automation Capabilities.

## Input

The user selects a BPMN element and describes:

- capability name;
- type;
- input variables;
- output variables;
- integration style;
- validation rules;
- deployment target.

Example:

```text
Task: Verificar dados da solicitacao
Input: orientador, estudante, tema, area
Output: complete, missingFields
```

## Generated REST service

```text
validate-request-service/
├── pom.xml
├── src/
├── Dockerfile
├── README.md
├── service-definition.yaml
└── tests/
```

## Generated External Task Worker

```text
validate-request-worker/
├── pom.xml
├── src/
├── Dockerfile
├── README.md
├── worker-definition.yaml
└── tests/
```

## Required generated contracts

- DTOs or JSON Schema for inputs and outputs.
- Capability metadata.
- Runtime configuration keys.
- Health endpoint.
- Test fixture.
- Dockerfile.
- Local run instructions.

## E01 boundary

E01 defines the architecture. Full code generation is deferred. Skeleton templates may be added later when ADE has a stable project model.

