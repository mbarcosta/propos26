# Data resolution plan

`DataResolutionPlan` is persisted in `AutomationProject.dataResolutionPlans`, keyed by BPMN element and target input. Supported strategies are `MAPPING`, `MESSAGE_EXTRACTION`, `TRANSFORMATION` and `CAPABILITY_LOOKUP`.

Example configuration (illustrative values, generated from current contracts):

```json
{
  "studentId": { "strategy": "CAPABILITY_LOOKUP", "source": "studentEmail", "capabilityId": "FIND_STUDENT_BY_EMAIL", "capabilityOutput": "id" },
  "advisorId": { "strategy": "CAPABILITY_LOOKUP", "source": "advisorEmail", "capabilityId": "FIND_PROFESSOR_BY_EMAIL", "capabilityOutput": "id" },
  "title": { "strategy": "MESSAGE_EXTRACTION" },
  "date": { "strategy": "MESSAGE_EXTRACTION" },
  "location": { "strategy": "MESSAGE_EXTRACTION" },
  "committeeMembers": { "strategy": "MESSAGE_EXTRACTION" }
}
```

The UI does not manufacture IDs. A lookup plan records its source, capability and output; execution still requires that lookup capability to be represented as an earlier executable task/worker.

