# E06 - Automation Configuration Wizard

E06 adds a guided Automation Configuration Wizard to the ADE.

The wizard works on the active `AutomationProject` and edits the same project structures used by the normal ADE areas:

- `requirements`
- `bindings`
- `variableMappings`
- `inboundConfigs`
- `outboundConfigs`
- `flowConditions`
- `deploymentConfiguration`

It does not create a parallel functional configuration model. The only wizard-specific state is navigation state in `wizardSession`.

## User Flow

```text
Open ADE
New/Open project
Model or import BPMN
Run Configuration Wizard
Configure elements step by step
Review global validation
Finish
READY_FOR_DEPLOYMENT
Deploy manually
```

## Scope

The implementation is deterministic. It uses BPMN element types, current configuration, capability metadata, and simple name matching. It does not use LLMs, prompts, or generative interpretation.

## Screenshots

Screenshots should be captured from the compose-managed ADE runtime after deployment of this sprint:

- Wizard modal over ADE backdrop.
- Service Task capability and mapping step.
- Inbound message/correlation step.
- Global validation step.
