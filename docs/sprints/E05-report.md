# E05 Report - ADE Project Management

## Scope

E05 turns the Automation Development Environment into a project-based workspace. The ADE no longer starts with the Vinculacao de Orientacao process opened. It starts with a home screen where the user explicitly creates or opens an Automation Project.

## Persistence Model

Projects are persisted in browser local storage under `propos26.ade.projects`. The saved object follows `platform/automation-development-environment/model/automation-project.schema.json`.

The persisted `AutomationProject` contains:

- `metadata`: `projectId`, `name`, `key`, `version`, and `description`.
- `bpmnXml`: the executable BPMN XML.
- `automationRequirements`: requirements derived from BPMN analysis.
- `capabilityBindings`: normalized binding records for BPMN element to capability.
- `bindings`: UI lookup map for active binding state.
- `variableMappings`: inbound mapping snapshots.
- `inboundIntegrations` and `inboundConfigs`: external event, Camunda message, correlation field, and variable mapping configuration.
- `outboundIntegrations` and `outboundConfigs`: outbound email/service configuration.
- `correlationDefinitions`: normalized correlation records derived from inbound integrations.
- `generatedComponents`: references to generated or planned workers/services.
- `deploymentConfiguration`: process and integration deployment settings.
- `deploymentHistory`: deployment result history placeholder.
- `flowConditions`: gateway branch/default configuration.
- `status`: design-time project status.

The design intentionally separates project state from Camunda runtime state. Closing or deleting an ADE project does not delete PPG Management data, Camunda definitions, or running instances.

## Interface

The ADE header now exposes a single left-side Project popup menu:

- Open
- New
- Save
- Close

When no project is open, the user sees:

```text
propos26 Automation Development Environment
Integrated with PPG Management
```

Project operations are reached through the left-side Project menu.

The active project is shown in the sidebar:

```text
Project: <name>
Version: <version>
Status: <status>
Saved | Unsaved changes
```

Workspace areas are:

- Project
- BPMN
- Automation
- Capabilities
- Integrations
- Deployment
- Execution

All project-specific controls are disabled when no project is open.

## Operations

`New` requests only the project name, derives the key from that name, sets version `1.0`, and creates a minimal executable BPMN model with start and end events.

`Open` lists saved projects with:

- name
- version
- lastModified
- deploymentStatus

`Save` stores the full project sidecar model and BPMN XML explicitly.

`Close` checks unsaved changes, offers saving, closes the active project, and returns to the start screen.

Only one project is open at a time. `New`, `Open`, and `Close` check the current project for unsaved changes and ask whether it should be saved before continuing.

`Delete Project` and `Save As` are not exposed in the simplified E05 project menu.

## Multiple Projects

On first use, the ADE seeds example projects for:

- Vinculacao de Orientacao
- Defesa de Mestrado

They are not opened automatically. This demonstrates coexistence while preserving the E05 rule that the ADE starts without a process loaded. Users can also create Cancelamento de Orientacao without ADE code changes.

## Unsaved Changes

Project metadata edits mark the project as dirty and update the sidebar to `Unsaved changes`. Save resets the marker to `Saved`. Close/open/delete flows check for unsaved changes before replacing or removing the active project.

## Execution

The Execution area can list Camunda process instances for the active project's `key` through:

```text
GET /api/execution/instances?processDefinitionKey=<projectKey>
```

It can cancel a running instance through:

```text
POST /api/execution/instances/{instanceId}/cancel
```

This cancels the instance only. It does not delete the Camunda process definition and does not undeploy the automation.

## Storage Limitations

Project storage is still browser-local. It is enough for the sprint validation flow but not durable across browsers or machines. A later sprint should move `AutomationProject` storage to an ADE backend repository with import/export and server-side validation against the JSON schema.

Deployment history is modeled in the project format, but the current UI stores only the latest deployment result display. A later iteration should append successful and failed deployment attempts to `deploymentHistory`.

The BPMN modeler still runs from CDN assets. A production ADE should pin frontend dependencies through a build pipeline.

## Recommendations

- Add backend project persistence and schema validation.
- Add explicit `Undeploy Automation` as a separate operation from `Delete Project`.
- Store deployment history entries after every deployment attempt.
- Add automated browser tests for create/save/close/open/delete and instance cancellation.
- Replace prompt-based project creation with a modal form once a frontend component structure exists.
