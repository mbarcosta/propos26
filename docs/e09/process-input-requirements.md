# Process input requirements

`DataRequirementResolver` compares each capability input with data visible before a task. Its statuses are `SATISFIED`, `MISSING`, `TYPE_MISMATCH`, `TRANSFORMATION_REQUIRED` and `LOOKUP_REQUIRED`. Direct mapping requires type compatibility; notably `body:String` is not compatible with `studentId:Long`. Integer-to-long/number widening is the only implicit numeric widening.

`ProcessInputRequirementAnalyzer` is independent from a domain process. Its BPMN adapter supplies service-task snapshots in graph order, including data visible on incoming paths, and the analyzer evaluates the current registry contract and persisted resolution plan. The browser's existing graph walk builds `ProcessDataContext` from earlier inbound fields, extraction outputs and capability outputs.

The Wizard filters strategies by context:

- mapping appears only when compatible prior data exists;
- extraction appears when an earlier email inbound event exists;
- transformation appears when source data exists and requires an explicit conversion;
- lookup appears when another capability has a compatible output.

Readiness is recalculated from current state after each edit. A changed fingerprint changes the project to `CONFIGURATION_REVIEW_REQUIRED` and creates a blocking diagnostic at the task.

