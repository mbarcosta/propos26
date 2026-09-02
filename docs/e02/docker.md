# E02 Docker

## propos26 Automation Platform

Root files:

```text
docker-compose.yml
.env.example
```

Commands:

```bash
docker compose build
docker compose up -d
docker compose logs
docker compose down
```

Services:

- `camunda7`
- `ade`
- `gms`
- `cir`
- `cpf-service`
- `automation-workers`

The root compose does not start PPG Management.

## PPG Management Service

Path:

```text
domain-systems/ppg-management-service
```

Commands:

```bash
docker compose build
docker compose up -d
docker compose logs
docker compose down
```

This compose starts only PPG Management.

## Environment

Do not commit `.env` files. Use `.env.example` as the template.

Key variables:

- `CAMUNDA_BASE_URL`
- `GMS_BASE_URL`
- `CIR_BASE_URL`
- `PPG_MANAGEMENT_BASE_URL`
- `GMS_MAIL_USERNAME`
- `GMS_MAIL_PASSWORD`

