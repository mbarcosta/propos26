# E09 report — runtime contracts and input data extraction

## Outcome

ADE now discovers provider operations at runtime, normalizes them independently of OpenAPI, exposes them through the existing Registry endpoint, and drives generic Wizard input requirements. Resolution plans and extraction contracts are persisted; CIR accepts extraction configuration without rebuild and applies a deterministic, typed provider.

## Architecture change

Previously `capabilities.json` was loaded on every registry call and the Wizard contained only its flattened fields. PPG-ADM had a repository `openapi.yaml`, but it was neither served nor sufficiently detailed for request/response discovery.

Now the flow is:

```text
PPG-ADM /v3/api-docs -> OpenApiContractDiscovery -> normalized Registry/cache
  -> Wizard + ProcessDataContext -> DataResolutionPlan
  -> InputExtractionContract -> CIR DataExtractionProvider -> Camunda variables
```

The root compose now starts PPG-ADM, registers its internal contract URL in ADE and persists ADE discovery cache and CIR extraction contracts.

## Actual provider contract

The runtime provider document is `domain-systems/ppg-management-service/src/main/resources/openapi-runtime.json`, version `0.4.0`. `CREATE_DEFENSE` declares required `studentId:Long`, `advisorId:Long`, `title:String`, `date:Date`, `location:String` and `committeeMembers:Array<CommitteeMember>`. `CommitteeMember` declares `name`, `email`, `institution` and `role`. The same document exposes email lookup operations, enabling explicit ID resolution rather than inventing IDs from message text.

## Wizard and readiness

The Capabilities screen has a refresh action and displays version/fingerprint/stale status. A service task displays provider-derived requirements and offers only context-valid Mapping, Extraction, Transformation and Lookup strategies. Extraction outputs immediately enter the calculated `ProcessDataContext`; task diagnostics are recalculated after every change. Contract fingerprint drift requires review.

Screenshots were not generated in this execution environment because no browser capture facility is attached. The implemented screen is in `static/index.html`/`app.js`; this is an explicit evidence gap rather than a fabricated artifact.

## Example artifacts

Representative `DataResolutionPlan`, CIR contract and structured email template are documented in `docs/e09/data-resolution-plan.md` and `docs/e09/input-extraction-contract.md`.

After extraction, a representative payload is:

```json
{
  "studentEmail": "student@example.org",
  "advisorEmail": "advisor@example.org",
  "title": "Runtime contracts",
  "date": "2026-11-05",
  "location": "Room 10",
  "committeeMembers": [{ "name": "Ada", "email": "ada@example.org", "institution": "IFES", "role": "MEMBER" }],
  "extractionValid": true
}
```

Lookup tasks then add `studentId:Long` and `advisorId:Long`; all required fields must be ready before invocation.

## Verification

- Production compilation passed for ADE, CIR and PPG-ADM with `mvn -q -DskipTests compile`.
- `node --check` passed for the Wizard JavaScript.
- `git diff --check` reported no whitespace errors.
- Tests were added for an unknown capability, nested `$ref`/array schemas, fingerprint evolution, typed extraction, required-field diagnostics and complex arrays.
- JUnit execution could not complete because Maven Central access fails in this environment (PKIX/connect timeout while resolving Surefire and Spring test artifacts). This is an environment limitation; it is not reported as a passing test run.

## Limitations

- The deterministic extractor requires a labelled structured template; arbitrary prose needs another `DataExtractionProvider` and is deliberately not inferred.
- Lookup plans identify the required operation but the BPMN still needs an executable lookup task/worker before the consuming task.
- OpenAPI discovery currently accepts provider-published JSON and local `$ref`; YAML and external references are intentionally rejected.
- The legacy, process-specific CIR parser remains as fallback for routes without an E09 extraction contract. It should be removed after existing configurations are migrated.
