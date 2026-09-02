# E02 Deployment

## States

The ADE uses:

- `DRAFT`
- `VALID`
- `DEPLOYING`
- `READY`
- `FAILED`

## Pipeline implemented

```text
Automation Project
  -> Validate BPMN
  -> Validate requirements/bindings
  -> Deploy BPMN to Camunda 7
  -> Apply CIR route configuration
  -> Deployment READY or FAILED
```

## Deferred

- GMS configuration publishing is still file/environment based.
- Service generation is not implemented.
- Full service availability probing is not complete.
- Instance monitoring is not complete.

