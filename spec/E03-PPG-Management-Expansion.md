# E03 — Expansão do PPG Management System

## 1. Objetivo

Expandir o **PPG Management System** para oferecer um conjunto mais completo de serviços de domínio que possam ser consumidos por processos automatizados através do propos26.

O PPG Management deve permanecer um sistema independente, executável via Docker e acessível por REST. O sistema **não deve conhecer Camunda, CIR, GMS ou ADE**.

## 2. Escopo funcional

Implementar ou consolidar funcionalidades para os domínios:

- estudantes;
- professores;
- orientações;
- defesas;
- documentos de dissertação.

## 3. Estudantes

Implementar:

```http
GET /api/students
GET /api/students/{id}
GET /api/students/by-registration/{registration}
GET /api/students/by-email?email={email}
POST /api/students
GET /api/students/{id}/status
```

Deve ser possível determinar se o estudante existe e está ativo.

## 4. Professores

Implementar:

```http
GET /api/professors
GET /api/professors/{id}
GET /api/professors/by-email?email={email}
POST /api/professors
```

## 5. Orientações — Advisorships

Implementar:

```http
GET  /api/advisorships
GET  /api/advisorships/{id}
GET  /api/advisorships/by-student/{studentId}
GET  /api/advisorships/by-advisor/{advisorId}
POST /api/advisorships
```

Adicionar status:

```text
ACTIVE
CANCELLED
COMPLETED
```

### Cancelamento

O cancelamento deve preservar histórico. Preferir:

```http
POST /api/advisorships/{id}/cancel
```

ou `PATCH` equivalente.

Não usar exclusão física como operação normal do processo de negócio. `DELETE` pode existir apenas para administração/testes, se necessário.

## 6. Defesas de Mestrado

Criar a entidade `Defense` com pelo menos:

```text
id
studentId
advisorId
title
date
location
status
committeeMembers
createdAt
updatedAt
```

Status:

```text
HOMOLOGATED
DEFENDED
FINALIZED
CANCELLED
```

Implementar:

```http
POST /api/defenses
GET  /api/defenses
GET  /api/defenses/{id}
GET  /api/defenses/by-student/{studentId}
PUT  /api/defenses/{id}
PATCH /api/defenses/{id}/status
POST /api/defenses/{id}/cancel
```

## 7. Banca

Criar `CommitteeMember` com:

```text
name
email
institution
role
```

Uma defesa deve aceitar múltiplos membros de banca.

## 8. Upload de dissertação

Implementar:

```http
POST /api/defenses/{defenseId}/dissertation
```

Usar `multipart/form-data`.

O upload deve:

1. validar a defesa;
2. armazenar o arquivo;
3. registrar metadados;
4. associar o documento à defesa;
5. devolver os metadados.

Metadados mínimos:

```text
documentId
defenseId
fileName
contentType
size
uploadedAt
version
```

## 9. Download e metadados

Implementar:

```http
GET /api/defenses/{defenseId}/dissertation
GET /api/defenses/{defenseId}/dissertation/download
```

A primeira operação retorna metadados; a segunda retorna o arquivo.

## 10. Link para download

Implementar:

```http
POST /api/defenses/{defenseId}/dissertation/download-link
```

ou solução REST equivalente.

Preparar a arquitetura para links temporários, controlados ou revogáveis. Segurança avançada pode ficar para sprint posterior.

## 11. Substituição e versão

Permitir substituição do documento antes da defesa. Preparar:

```text
REPLACE_DISSERTATION
LIST_DISSERTATION_VERSIONS
```

A implementação pode ser simples, desde que preserve a evolução futura.

## 12. Persistência de arquivos

O arquivo pertence ao **PPG Management**.

Camunda deve guardar somente referências como:

```text
documentId
defenseId
downloadUrl
```

Não armazenar binários em variáveis Camunda.

## 13. Interface web

Atualizar a interface mínima para visualizar:

- estudantes;
- professores;
- orientações;
- defesas;
- banca;
- documentos da defesa.

Na tela da defesa permitir upload/substituição e download da dissertação.

## 14. Docker

O PPG Management deve continuar independente:

```bash
docker compose build
docker compose up -d
docker compose down
```

Usar volumes para persistência de banco e documentos quando adequado.

## 15. OpenAPI

Atualizar/criar documentação OpenAPI/Swagger para os endpoints.

## 16. Testes

Criar testes para:

- busca de estudante;
- busca de professor;
- busca de orientação por aluno;
- busca por orientador;
- cancelamento de orientação;
- criação e atualização de defesa;
- mudança de status;
- upload;
- download;
- geração de link.

## 17. Critérios de aceitação

A sprint termina quando:

1. o PPG Management subir sozinho via Docker;
2. estudantes puderem ser consultados por matrícula/e-mail;
3. professores puderem ser consultados por e-mail;
4. orientações puderem ser consultadas por estudante e orientador;
5. orientação puder ser cancelada sem apagar histórico;
6. defesa puder ser cadastrada e atualizada;
7. status puder ser alterado;
8. banca puder ser armazenada;
9. dissertação puder ser enviada;
10. dissertação puder ser baixada;
11. puder ser obtida referência/link de download;
12. arquivos e dados permanecerem sob responsabilidade do PPG Management;
13. nenhuma dependência do Camunda/ADE tiver sido introduzida.

## 18. Relatório

Criar:

```text
docs/sprints/E03-report.md
```

Documentar endpoints, modelo de dados, banco, armazenamento de arquivos, testes, limitações e decisões pendentes.
