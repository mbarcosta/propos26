# Infrastructure

Initial infrastructure area for propos26.

E01 target:

- define local Docker Compose strategy;
- keep credentials out of the repository;
- use `.env.example` as the documented contract for environment variables;
- support Camunda 7, GMS, CIR, ADE, PPG Management Service, and Automation Services progressively.

No compose file is introduced in E01 because only `cpf-service` currently has a Dockerfile.

