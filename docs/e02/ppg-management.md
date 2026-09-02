# PPG Management MVP

Path:

```text
domain-systems/ppg-management-service
```

## Independence

The PPG Management Service:

- does not know Camunda;
- does not know GMS;
- does not know CIR;
- does not know ADE;
- has no Maven dependency on propos26 services.

## Domain model

Student:

- `id`
- `registration`
- `name`
- `email`
- `status`

Professor:

- `id`
- `name`
- `email`
- `status`

Advisorship:

- `id`
- `studentId`
- `advisorId`
- `title`
- `researchArea`
- `startDate`
- `status`

## API

```http
GET /api/health
GET /api/students
GET /api/students/{id}
POST /api/students
GET /api/professors
GET /api/professors/{id}
POST /api/professors
GET /api/advisorships
GET /api/advisorships/{id}
POST /api/advisorships
```

## UI

```text
http://localhost:8090
```

The UI shows Students, Professors, and Advisorships.

## Storage

E02 persists data in `PPG_DATA_FILE`, backed by a Docker volume. JPA/relational persistence remains an E03 hardening item.

