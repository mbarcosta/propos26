# Input extraction contract

When message extraction is selected, ADE derives a typed contract from the selected capability schema and associates it with the earlier inbound event. The user chooses sources in ordinary controls; JSON and expressions are not the primary UI.

```json
{
  "event": "DEFENSE_REQUESTED",
  "channel": "EMAIL",
  "sourcePart": "BODY",
  "strategy": "STRUCTURED_TEMPLATE",
  "contractFingerprint": "...",
  "outputs": [
    { "name": "title", "type": "String", "required": true },
    { "name": "date", "type": "Date", "required": true },
    { "name": "committeeMembers", "type": "Array<CommitteeMember>", "required": true,
      "properties": [
        { "name": "name", "type": "String", "required": true },
        { "name": "email", "type": "String", "required": true },
        { "name": "institution", "type": "String", "required": true },
        { "name": "role", "type": "String", "required": true }
      ] }
  ]
}
```

`StructuredTemplateExtractor` is the deterministic initial `DataExtractionProvider`. It parses labelled scalar fields and repeated array-object blocks, validates Long/Integer/Decimal/Boolean/ISO Date/ISO DateTime, and returns comprehensible diagnostics for absent or invalid required fields. It does not claim to understand arbitrary free text.

The generic template for the example is:

```text
title: <title>
date: <date>
location: <location>
committeeMembers:
  - name: <name>
    email: <email>
    institution: <institution>
    role: <role>
```

