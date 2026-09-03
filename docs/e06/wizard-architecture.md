# Wizard Architecture

The wizard is implemented in the ADE frontend as reusable functions over the existing project model.

## Runtime Structure

```text
AutomationProject
  wizardSession
    projectId
    currentStep
    status
    startedAt
    lastUpdatedAt
    lastRunAt
    steps
    stepStatuses
```

`wizardSession` stores only navigation and progress metadata. Functional data remains in the normal project fields.

## Components

- `startWizard`
- `buildWizardSteps`
- `renderWizard`
- `renderWizardProgress`
- `renderWizardStep`
- `renderServiceTaskWizard`
- `renderOutboundWizard`
- `renderInboundWizard`
- `renderGatewayWizard`
- `validateWizardStep`
- `globalWizardValidation`

These functions act as component boundaries inside the current static frontend stack.

## Synchronization

Wizard changes call the same persistence functions used by normal ADE screens:

- capability selection updates `state.bindings`
- variable mappings update `state.variableMappings`
- inbound message/correlation updates `state.inboundConfigs`
- outbound communication updates `state.outboundConfigs`
- gateway conditions update `state.flowConditions`

Normal screens are re-rendered after wizard changes, and rerunning the wizard reads current values.
