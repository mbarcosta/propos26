# E04 Report - Dynamic Capabilities and PPG Management Integration

## Scope

E04 evolves the ADE into a project environment explicitly integrated with PPG Management. The UI keeps the identity:

```text
propos26 Automation Development Environment
Integrated with PPG Management
```

The ADE remains generic. It works with Automation Projects, BPMN elements, requirements, capabilities, bindings, implementations, mappings, and deployments. No process-specific screen or class was added for Cancelamento de Orientacao.

## Capability Registry

The capability registry is now loaded from:

```text
platform/automation-development-environment/src/main/resources/config/capabilities.json
```

The backend endpoint remains:

```text
GET /api/capabilities
```

Each capability includes:

- id
- name
- description
- type
- provider
- interfaceType
- endpoint/topic
- inputParameters
- outputParameters
- implementationType
- implementation
- deployment
- status

## PPG Management Capabilities

The registry includes capabilities for:

- Student: `FIND_STUDENT`, `FIND_STUDENT_BY_EMAIL`, `VALIDATE_STUDENT`
- Professor: `FIND_PROFESSOR`, `FIND_PROFESSOR_BY_EMAIL`
- Advisorship: `CREATE_ADVISORSHIP`, `FIND_ADVISORSHIP`, `FIND_ADVISORSHIP_BY_STUDENT`, `FIND_ADVISORSHIPS_BY_ADVISOR`, `CHECK_ADVISORSHIP`, `CANCEL_ADVISORSHIP`
- Defense: `CREATE_DEFENSE`, `GET_DEFENSE`, `FIND_DEFENSE_BY_STUDENT`, `UPDATE_DEFENSE`, `CHANGE_DEFENSE_STATUS`, `CANCEL_DEFENSE`
- Dissertation Document: `UPLOAD_DISSERTATION`, `GET_DISSERTATION_METADATA`, `DOWNLOAD_DISSERTATION`, `GENERATE_DISSERTATION_DOWNLOAD_LINK`, `REPLACE_DISSERTATION`

`UPLOAD_DISSERTATION` and `REPLACE_DISSERTATION` explicitly model `multipart/form-data` with `file` inputs.

## Dynamic Binding

After BPMN analysis, each automatable BPMN element can select any capability from the registry. The screen does not hard-code process-specific choices. When a capability is selected, the ADE shows its contract and implementation details next to the BPMN requirement.

For Camunda execution, service/send tasks are configured as External Tasks using the selected capability id as the topic. This allows a generated or reused worker to subscribe to that capability topic.

## Variable Mappings

The Automation panel shows mapping fields derived from each capability contract:

- input parameter mappings from Camunda process variables
- output parameter mappings back into process variables

Mappings are persisted in the `AutomationProject` under `variableMappings` and normalized into each `capabilityBinding`.

This supports flows such as:

```text
GENERATE_DISSERTATION_DOWNLOAD_LINK
        output downloadUrl -> ${downloadUrl}
SEND_EMAIL
        input body <- ${downloadUrl}
```

## Implementations

For each selected capability, the ADE displays:

- Capability
- Provider
- Implementation Type
- Implementation
- Deployment
- Status

The registry distinguishes existing REST services and External Task workers from generated workers/services.

## Worker Generation and Code View

The Automation panel provides:

```text
Generate/Customize Worker
View Code
```

For REST capabilities, the ADE generates a project metadata representation of a worker that:

- receives Camunda variables;
- builds a request from input mappings;
- calls the configured endpoint;
- maps response fields to output variables;
- returns the outputs that an External Task completion step would send back to Camunda.

Generated code can be viewed in the UI for:

- `pom.xml`
- main class
- worker class
- REST client
- DTOs
- application configuration
- Dockerfile
- README
- `service-definition.yaml`

The current sprint stores generated artifacts inside the project metadata rather than writing them to the filesystem.

## Validation Scenario

`Cancelamento de Orientacao` can be created as a normal ADE project. The user can model/import BPMN, analyze requirements, select `CANCEL_ADVISORSHIP`, map `advisorshipId <- ${advisorship.id}`, map outputs such as `status -> ${advisorshipStatus}`, generate/customize a worker, configure integrations/correlations, save, and deploy.

No ADE code path is specific to that process.

## Limitations

- Capability configuration is file-backed in the ADE package, not yet editable from an admin UI.
- Generated worker artifacts are shown and persisted in project metadata, but not materialized as a standalone source directory.
- The generated REST client is a scaffold for review/customization, not complete executable integration code.
- Backend validation does not yet validate mappings against the capability contract schema.
- Existing worker reuse is represented by capability metadata; automatic runtime discovery is not yet implemented.

## Technical Debt

- Add server-side project persistence and schema validation.
- Add a backend generator that writes `pom.xml`, `src/`, `Dockerfile`, `README.md`, and `service-definition.yaml` into a generated component workspace.
- Validate capability input/output mappings before deployment.
- Add tests for registry loading, capability rendering, mapping persistence, and code generation.
