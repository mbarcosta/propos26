# PPG Management Service

Independent domain system for the E02 MVP.

It owns durable academic state and has no dependency on Camunda, GMS, CIR, ADE, or propos26 internal classes.

## Run locally

```bash
mvn spring-boot:run
```

Default URL:

```text
http://localhost:8090
```

## Docker

```bash
docker compose build
docker compose up -d
docker compose logs -f
docker compose down
```

## API

- `GET /api/health`
- `GET /api/students`
- `GET /api/students/{id}`
- `GET /api/students/by-registration/{registration}`
- `GET /api/students/by-email?email={email}`
- `GET /api/students/{id}/status`
- `POST /api/students`
- `PUT /api/students/{id}`
- `DELETE /api/students/{id}`
- `GET /api/professors`
- `GET /api/professors/{id}`
- `GET /api/professors/by-email?email={email}`
- `POST /api/professors`
- `PUT /api/professors/{id}`
- `DELETE /api/professors/{id}`
- `GET /api/advisorships`
- `GET /api/advisorships/{id}`
- `GET /api/advisorships/by-student/{studentId}`
- `GET /api/advisorships/by-advisor/{advisorId}`
- `POST /api/advisorships`
- `POST /api/advisorships/{id}/cancel`
- `GET /api/defenses`
- `GET /api/defenses/{id}`
- `GET /api/defenses/by-student/{studentId}`
- `POST /api/defenses`
- `PUT /api/defenses/{id}`
- `PATCH /api/defenses/{id}/status`
- `POST /api/defenses/{id}/cancel`
- `POST /api/defenses/{defenseId}/dissertation`
- `GET /api/defenses/{defenseId}/dissertation`
- `GET /api/defenses/{defenseId}/dissertation/versions`
- `GET /api/defenses/{defenseId}/dissertation/download`
- `POST /api/defenses/{defenseId}/dissertation/download-link`

OpenAPI:

```text
openapi.yaml
```

## UI

Open:

```text
http://localhost:8090
```

## E02 storage note

The MVP persists data to a JSON file under `PPG_DATA_FILE` and dissertation binaries under `PPG_DOCUMENTS_DIR`. The architecture still keeps all domain state and files outside Camunda.
