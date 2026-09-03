# E03 Report - PPG Management Expansion

## Scope

E03 expands the independent PPG Management Service so automated processes can consume a broader domain API without coupling the service to Camunda, CIR, GMS, or ADE.

## Service Boundary

PPG Management remains a standalone Spring Boot REST/UI service under:

```text
domain-systems/ppg-management-service
```

It owns academic domain data and dissertation files. Process engines should store only references such as `defenseId`, `documentId`, and `downloadUrl`.

## Data Model

Existing entities were consolidated:

- `Student`: id, registration, name, email, status.
- `Professor`: id, name, email, status.
- `Advisorship`: id, studentId, advisorId, title, researchArea, startDate, status.
- `Program`: program metadata.

New entities:

- `Defense`: id, studentId, advisorId, title, date, location, status, committeeMembers, createdAt, updatedAt.
- `CommitteeMember`: name, email, institution, role.
- `DissertationDocument`: documentId, defenseId, fileName, contentType, size, uploadedAt, version, storagePath.

Advisorship status values used by the API:

```text
ACTIVE
CANCELLED
COMPLETED
```

Defense status values used by the API:

```text
HOMOLOGATED
DEFENDED
FINALIZED
CANCELLED
```

## Persistence

The E03 implementation keeps JSON-file persistence for domain state:

```text
PPG_DATA_FILE
```

Dissertation binaries are stored separately under:

```text
PPG_DOCUMENTS_DIR
```

The Docker Compose file defines separate volumes for state and documents.

## Endpoints

Students:

- `GET /api/students`
- `GET /api/students/{id}`
- `GET /api/students/by-registration/{registration}`
- `GET /api/students/by-email?email={email}`
- `GET /api/students/{id}/status`
- `POST /api/students`
- `PUT /api/students/{id}`
- `DELETE /api/students/{id}` for administration/tests.

Professors:

- `GET /api/professors`
- `GET /api/professors/{id}`
- `GET /api/professors/by-email?email={email}`
- `POST /api/professors`
- `PUT /api/professors/{id}`
- `DELETE /api/professors/{id}` for administration/tests.

Advisorships:

- `GET /api/advisorships`
- `GET /api/advisorships/{id}`
- `GET /api/advisorships/by-student/{studentId}`
- `GET /api/advisorships/by-advisor/{advisorId}`
- `POST /api/advisorships`
- `POST /api/advisorships/{id}/cancel`

Defenses:

- `POST /api/defenses`
- `GET /api/defenses`
- `GET /api/defenses/{id}`
- `GET /api/defenses/by-student/{studentId}`
- `PUT /api/defenses/{id}`
- `PATCH /api/defenses/{id}/status`
- `POST /api/defenses/{id}/cancel`

Dissertations:

- `POST /api/defenses/{defenseId}/dissertation`
- `GET /api/defenses/{defenseId}/dissertation`
- `GET /api/defenses/{defenseId}/dissertation/download`
- `POST /api/defenses/{defenseId}/dissertation/download-link`
- `GET /api/defenses/{defenseId}/dissertation/versions`

## OpenAPI

The API is documented in:

```text
domain-systems/ppg-management-service/openapi.yaml
```

## Web UI

The static UI at `http://localhost:8090` now shows:

- students;
- professors;
- advisorships;
- defenses;
- committee member data on defenses;
- dissertation upload/replacement and metadata/download link retrieval.

## Tests

Tests cover:

- student lookup by registration and email;
- student active status endpoint;
- professor lookup by email;
- advisorship lookup by student and advisor;
- advisorship cancellation without physical deletion;
- defense creation and update;
- defense status change and cancellation;
- dissertation upload;
- dissertation metadata lookup;
- dissertation download;
- dissertation download-link generation.

## Limitations

- Persistence is still JSON plus filesystem, not JPA/relational.
- Status values are represented as strings; enum validation can be hardened later.
- Download links are stable direct REST URLs. The response shape is prepared for future temporary/revocable links, but advanced security is deferred.
- `DELETE` remains available only for administration/tests and should not be used as the normal process operation for advisorships.

## Pending Decisions

- Whether E04 should migrate PPG Management to JPA and a relational database.
- Whether file links should become signed, expiring, and revocable.
- Whether committee member roles should become controlled enum values.
