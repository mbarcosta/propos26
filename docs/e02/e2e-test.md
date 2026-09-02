# E02 E2E Test

## Start PPG Management

```bash
cd domain-systems/ppg-management-service
docker compose build
docker compose up -d
```

Open:

```text
http://localhost:8090
```

Create initial data:

```bash
curl -X POST http://localhost:8090/api/students \
  -H "Content-Type: application/json" \
  -d "{\"registration\":\"2026001\",\"name\":\"Maria Silva\",\"email\":\"maria@example.com\",\"status\":\"ACTIVE\"}"

curl -X POST http://localhost:8090/api/professors \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Joao Souza\",\"email\":\"joao@example.com\",\"status\":\"ACTIVE\"}"
```

## Start propos26 Automation Platform

From repository root:

```bash
docker compose build
docker compose up -d
```

Open ADE:

```text
http://localhost:8070
```

## Configure and deploy

1. Load the reference project.
2. Open BPMN.
3. Analyze BPMN.
4. Bind service/send elements to capabilities.
   - Bind outbound Send Tasks to `SEND_EMAIL`.
   - Message throw events are listed for configuration; use Send Task or Service Task when the notification must execute through an External Task worker.
5. Save integration/correlation.
6. Validate.
7. Deploy Automation.

Expected result:

```text
Status: READY
```

## Runtime exercise

The intended full runtime flow is:

```text
GMS -> CIR -> Camunda -> workers -> PPG Management
```

Current MVP can deploy BPMN and publish CIR route configuration. Full email-driven execution depends on a real GMS mailbox and Camunda runtime availability. The worker integration can be verified by completing a Camunda external task with variables or by running the process once Camunda has created the `REGISTER_ADVISORSHIP` task.

## Verify domain result

```bash
curl http://localhost:8090/api/advisorships
```

The PPG UI should show an advisorship with:

```text
status = IN_PROGRESS
```
