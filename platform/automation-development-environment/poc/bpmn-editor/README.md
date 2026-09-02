# BPMN Editor PoC

Static spike for embedding a BPMN editor in ADE.

Open `index.html` in a browser. It loads `bpmn-js` from a CDN, imports the default BPMN XML, lets the user edit the diagram, and exports XML to the text area.

E01 limitation:

- This spike does not pin npm dependencies or bundle assets.
- Camunda 7 properties panel integration is documented as the next validation step.
- A production ADE should use `camunda-bpmn-js` or `bpmn-js` plus Camunda moddle/properties panel dependencies through a normal frontend build.

