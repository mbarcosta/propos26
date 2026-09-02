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
- `POST /api/students`
- `PUT /api/students/{id}`
- `DELETE /api/students/{id}`
- `GET /api/professors`
- `GET /api/professors/{id}`
- `POST /api/professors`
- `PUT /api/professors/{id}`
- `DELETE /api/professors/{id}`
- `GET /api/advisorships`
- `GET /api/advisorships/{id}`
- `POST /api/advisorships`

## UI

Open:

```text
http://localhost:8090
```

## E02 storage note

The MVP persists data to a JSON file under `PPG_DATA_FILE` so it can run independently with a Docker volume and without introducing a database dependency during the operational spike. The architecture still keeps the domain state outside Camunda. Migrating this store to JPA and a relational database is an E03 hardening task.
