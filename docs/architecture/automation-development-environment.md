# Automation Development Environment

ADE is the IDE of process automation in propos26.

## Responsibilities

- Create Automation Projects.
- Create or import BPMN.
- Edit BPMN using an embedded modeler.
- Validate BPMN.
- Analyze BPMN elements and identify Automation Requirements.
- Query the Capability Registry.
- Bind BPMN tasks/events to existing capabilities.
- Configure External Task topics and REST integrations.
- Configure email input, outbound messages, variables, messages, and correlations.
- Generate service skeletons when no capability exists.
- Deploy automation units.
- Execute test instances.
- Monitor process instances, logs, messages, incidents, and External Tasks.

## Initial conceptual model

```text
AutomationProject
ProcessModel
BpmnElement
AutomationRequirement
AutomationCapability
ServiceDefinition
ServiceImplementation
IntegrationBinding
InboundEventDefinition
OutboundMessageDefinition
CorrelationDefinition
Deployment
ExecutionEnvironment
```

Traceability:

```text
BPMN Element
  -> Automation Requirement
  -> Capability
  -> Implementation
  -> Deployment
```

## AutomationRequirement

An `AutomationRequirement` is a need detected from BPMN or user configuration.

Examples:

- `SERVICE_IMPLEMENTATION`
- `INBOUND_EVENT`
- `CORRELATION_RULE`
- `MESSAGE_DEFINITION`
- `OUTBOUND_COMMUNICATION`
- `VARIABLE_MAPPING`
- `DEPLOYMENT_CONFIGURATION`

## ADE sidecar metadata

The ADE should store automation metadata outside the BPMN when the engine does not need it directly. BPMN remains the execution model; ADE metadata explains how to complete, deploy, test, and operate that model.

