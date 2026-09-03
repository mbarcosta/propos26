# Wizard Validation

Validation is split into step validation and global validation.

## Step Validation

Service tasks require:

- selected capability;
- all capability inputs mapped;
- outputs mapped when relevant.

Outbound communication requires:

- outbound capability;
- recipient;
- subject;
- body warning when empty.

Inbound events require:

- channel;
- external event;
- Camunda message;
- correlation field;
- correlation expression;
- valid variable mappings JSON.

Exclusive gateways require:

- condition expressions for non-default branches;
- default branch when conditions are incomplete.

## Global Validation

The final step runs all wizard step validations and the existing ADE `validateProject()` routine.

Readiness states:

- `READY`: no blocking errors.
- `NOT READY`: one or more blocking errors.

Finishing sets project status to `READY_FOR_DEPLOYMENT`. Later project changes move status to `CONFIGURATION_REVIEW_REQUIRED`.
