# Embedded BPMN Modeler

## Goal

ADE should include a BPMN editor compatible with Camunda 7 so users can model and configure automations without constantly switching tools.

## Options

### `bpmn-js`

Base BPMN 2.0 rendering and editing toolkit.

Strengths:

- Mature browser-based modeler.
- Good fit for an embedded custom UI.
- Works with BPMN XML.

Limitations:

- Camunda-specific properties require moddle descriptors and a properties panel.

### `camunda-bpmn-js`

Camunda-maintained distribution around `bpmn-js` for Camunda modeling experiences.

Strengths:

- Best starting point for Camunda-specific modeling.
- Aligns with Camunda 7/Platform modeling semantics.

Limitations:

- Package/version selection must be validated against the exact Camunda 7 property panel requirements.

### `bpmn-js-properties-panel`

Properties panel for editing BPMN element properties.

Strengths:

- Required for a practical editor experience.
- Can be extended with custom ADE panels for Automation Requirements and Capability Bindings.

Limitations:

- Camunda 7 properties require the correct Camunda provider/moddle setup.

### `bpmn-moddle`

Reads and writes BPMN XML and supports extension descriptors.

Strengths:

- Necessary for XML parsing/export.
- Enables Camunda extension attributes through `camunda-bpmn-moddle`.

Limitations:

- Not a UI by itself.

## Recommendation

Use an embedded web modeler based on:

- `camunda-bpmn-js` when it provides the needed Camunda 7 distribution.
- `bpmn-js` + `bpmn-js-properties-panel` + `camunda-bpmn-moddle` if finer control is required.

ADE should keep Camunda-specific BPMN configuration in the editor, but ADE-specific metadata should not be forced into Camunda extensions unless the engine needs it at runtime. Prefer an ADE sidecar model for requirements, capabilities, deployment manifests, and test definitions.

## Spike result for E01

A minimal static spike was added under `platform/automation-development-environment/poc/bpmn-editor`. It demonstrates the intended user flow:

- load BPMN XML;
- render a BPMN modeler;
- edit the diagram;
- export the resulting BPMN XML.

The spike references browser packages from a CDN to avoid introducing a build system in E01. A production ADE should replace this with pinned npm dependencies and a real frontend build.

