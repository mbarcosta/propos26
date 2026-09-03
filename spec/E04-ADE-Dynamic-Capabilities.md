# E04 — Capabilities Dinâmicas do ADE e Integração com PPG Management

## 1. Objetivo

Evoluir o **Automation Development Environment — ADE** para funcionar explicitamente como:

> **ADE integrado ao PPG Management**

A distribuição atual é voltada ao domínio de pós-graduação, mas a arquitetura deve permitir futuras distribuições do ADE ligadas a outros sistemas de domínio.

O ADE não deve conhecer processos específicos antecipadamente. Deve conhecer capabilities, contratos, integrações e mecanismos de automação.

## 2. Identidade do ambiente

A interface deve apresentar claramente:

```text
propos26 Automation Development Environment
Integrated with PPG Management
```

ou equivalente.

A integração deve ser configurável em tempo de projeto/distribuição do ADE.

## 3. Capability Registry

Evoluir o registry para representar dinamicamente operações do PPG Management.

### Student

```text
FIND_STUDENT
FIND_STUDENT_BY_EMAIL
VALIDATE_STUDENT
```

### Professor

```text
FIND_PROFESSOR
FIND_PROFESSOR_BY_EMAIL
```

### Advisorship

```text
CREATE_ADVISORSHIP
FIND_ADVISORSHIP
FIND_ADVISORSHIP_BY_STUDENT
FIND_ADVISORSHIPS_BY_ADVISOR
CHECK_ADVISORSHIP
CANCEL_ADVISORSHIP
```

### Defense

```text
CREATE_DEFENSE
GET_DEFENSE
FIND_DEFENSE_BY_STUDENT
UPDATE_DEFENSE
CHANGE_DEFENSE_STATUS
CANCEL_DEFENSE
```

### Dissertation Document

```text
UPLOAD_DISSERTATION
GET_DISSERTATION_METADATA
DOWNLOAD_DISSERTATION
GENERATE_DISSERTATION_DOWNLOAD_LINK
REPLACE_DISSERTATION
```

## 4. Seleção dinâmica

Ao selecionar um elemento automatizável do BPMN, o usuário deve poder escolher uma capability do catálogo.

A lista deve vir do registry/configuração, nunca de código específico da tela ou processo.

## 5. Contratos

Mostrar para cada capability:

```text
name
description
provider
interface
endpoint/topic
input parameters
output parameters
```

Exemplo:

```text
Capability:
CANCEL_ADVISORSHIP

Provider:
PPG Management

Interface:
REST

Input:
advisorshipId : Long

Output:
id
status
updatedAt
```

## 6. Mapeamento de variáveis

Permitir mapear variáveis do processo para inputs e outputs da capability.

Exemplo:

```text
advisorshipId ← ${advisorship.id}
status → ${advisorshipStatus}
```

Os mappings devem ser persistidos no projeto.

## 7. Workers e serviços explícitos

Os workers/serviços não devem permanecer transparentes e fixos.

Para cada capability permitir visualizar:

```text
Capability
Provider
Implementation Type
Implementation
Deployment
Status
```

Tipos:

```text
REST_SERVICE
EXTERNAL_TASK_WORKER
GENERATED_WORKER
GENERATED_SERVICE
```

## 8. Geração/customização de workers

Introduzir a primeira versão do mecanismo:

```text
BPMN Service Task
     ↓
Capability
     ↓
Generate/Customize Worker
```

Para uma capability REST, o worker gerado deve:

1. receber variáveis do Camunda;
2. montar a chamada;
3. chamar o endpoint;
4. interpretar a resposta;
5. mapear saídas;
6. completar a External Task.

## 9. Visualização de código

Adicionar:

```text
View Code
```

Permitir visualizar pelo menos:

- classe principal;
- cliente REST;
- DTOs;
- configuração;
- Dockerfile.

Edição completa de código não é obrigatória nesta sprint.

## 10. Reuso

Antes de gerar um componente:

```text
Automation Requirement
        ↓
Select Capability
        ↓
Implementation exists?
    /             \
   YES             NO
    ↓               ↓
Reuse         Generate/Customize
```

## 11. Capability de upload

`UPLOAD_DISSERTATION` deve suportar conceitualmente `multipart/form-data`.

Inputs:

```text
defenseId
file
```

Outputs:

```text
documentId
fileName
version
downloadUrl
```

## 12. Link para banca

Adicionar `GENERATE_DISSERTATION_DOWNLOAD_LINK`.

Permitir composição:

```text
GENERATE_DISSERTATION_DOWNLOAD_LINK
              ↓
        downloadUrl
              ↓
          SEND_EMAIL
```

## 13. Processo desconhecido como validação

Usar:

```text
Cancelamento de Orientação
```

O ADE não deve possuir código específico desse processo.

O usuário deve conseguir:

1. criar o projeto;
2. modelar/importar BPMN;
3. selecionar capabilities;
4. configurar mappings;
5. configurar mensagens;
6. configurar correlações;
7. gerar/customizar worker quando necessário;
8. realizar deployment.

## 14. Genericidade

Não criar classes/telas específicas do processo. Trabalhar com:

```text
AutomationProject
BpmnElement
AutomationRequirement
Capability
CapabilityBinding
Implementation
VariableMapping
Deployment
```

## 15. Docker

Workers/serviços gerados devem ser preparados para Docker.

Quando aplicável gerar:

```text
pom.xml
src/
Dockerfile
README.md
service-definition.yaml
```

Não é obrigatório push automático para registry remoto.

## 16. Critérios de aceitação

A sprint termina quando:

1. o ADE mostrar a integração com PPG Management;
2. o registry possuir as novas capabilities;
3. capabilities puderem ser escolhidas dinamicamente;
4. contratos forem exibidos;
5. mappings puderem ser configurados;
6. mappings forem persistidos;
7. implementações forem visíveis;
8. o ADE puder gerar/customizar ao menos um worker;
9. o código gerado puder ser visualizado;
10. multipart estiver representado;
11. saída de capability puder alimentar uma tarefa posterior;
12. Cancelamento de Orientação puder ser configurado sem modificar o núcleo do ADE.

## 17. Relatório

Criar:

```text
docs/sprints/E04-report.md
```

Documentar capabilities, registry, bindings, geração, código gerado, teste do processo, limitações e dívida técnica.
