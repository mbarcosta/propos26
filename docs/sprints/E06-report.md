# E06 Report - Automation Configuration Wizard

## Summary

E06 adds a visual Automation Configuration Wizard to the ADE. The wizard is process-driven: it analyzes the active BPMN model, creates ordered steps, guides configuration element by element, validates each step, and finishes only when global validation has no blocking errors.

The wizard does not use generative AI.

## Architecture

The wizard reads and updates the active `AutomationProject`. Functional configuration remains in the existing ADE structures:

- `requirements`
- `bindings`
- `variableMappings`
- `inboundConfigs`
- `outboundConfigs`
- `flowConditions`
- `deploymentConfiguration`

The only wizard-owned data is `wizardSession`, which stores current step, timestamps, step list, and statuses.

## UI

The wizard is implemented as a centered modal with backdrop, header, progress navigation, scrollable content, and fixed footer. Footer actions are:

- Back
- Cancel
- Next
- Finish

Step states are shown with text and symbols for current, configured, warning, error, and pending states.

Screenshots were not captured in this execution because the ADE runtime is compose-managed by the user. See `docs/e06/overview.md` for the required screenshot checklist.

## BPMN Analysis

Supported wizard elements:

- Start message event
- Service task
- Send task
- Receive task
- Intermediate message catch event
- Intermediate message throw event
- Exclusive gateway
- Message end event

Steps are ordered by BPMN XML document order and followed by a global validation step.

## Configurators

Service task steps support:

- deterministic capability suggestion;
- rich capability picker;
- capability contract display;
- implementation display;
- input mappings;
- output mappings;
- worker generation/code view.

Outbound steps support:

- capability selection;
- recipient;
- subject;
- body/template;
- expected response.

Inbound/start message steps support:

- channel;
- provider;
- router;
- external event;
- Camunda message;
- correlation field;
- target process variable;
- initial variable mappings JSON.

Gateway steps validate branch conditions and default branch configuration.

## Persistence and Synchronization

Wizard edits immediately save to the same in-memory project model and local project persistence used by the normal ADE screens. The Automation tab is re-rendered after wizard changes.

Rerunning the wizard reads existing configuration rather than overwriting it.

## Validation

Step validation classifies findings as:

- ERROR
- WARNING
- INFO

Blocking errors disable Next. The global validation step also runs the existing ADE project validation.

Finish is enabled only when no blocking errors exist. Finish sets the project status to:

```text
READY_FOR_DEPLOYMENT
```

Later project changes move it to:

```text
CONFIGURATION_REVIEW_REQUIRED
```

## Tests

Executed:

- JavaScript syntax validation with `node --check`.
- JSON schema parse validation.
- ADE Maven compile with tests skipped.

`mvn test` remains blocked by the existing Maven Central PKIX error when downloading Surefire artifacts.

## Limitations

- Step ordering uses document order, not graph traversal.
- Service availability is reported as runtime dependency information; active health checks were not added to avoid coupling this sprint to live compose state.
- The modal is implemented in the current static frontend rather than a dedicated component framework.
- Worker generation is still metadata/code-preview oriented, matching E04.

## Recommendations for E07

- Add automated browser tests for wizard navigation and persistence.
- Add graph-based step ordering from BPMN start events.
- Add non-blocking runtime health checks for Camunda, PPG Management, GMS, and CIR.
- Materialize generated worker artifacts into a filesystem workspace.
- Add schema-level validation of capability mappings before deployment.
