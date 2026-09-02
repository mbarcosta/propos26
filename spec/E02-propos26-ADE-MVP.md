# E02 — MVP Operacional do Ambiente de Automação e Integração com Sistema de Domínio

## 1. Contexto

O Sprint E01 definiu a arquitetura do ecossistema **propos26** como uma plataforma orientada à automação de processos de negócio, baseada em:

- modelagem BPMN;
- Camunda 7 como motor de processos;
- GMS como gateway de e-mail;
- CIR como roteador de eventos para processos;
- serviços de automação;
- um **Automation Development Environment — ADE** como ambiente central de configuração e implantação;
- sistemas de domínio independentes;
- execução predominantemente baseada em serviços e containers Docker.

O Sprint E02 deverá transformar essa arquitetura em uma **primeira versão operacional do ambiente de automação**.

O objetivo não é simplesmente implementar o processo de Vinculação de Orientação.

O objetivo é criar uma versão mínima do ADE que permita configurar, implantar e executar essa automação utilizando os componentes do ecossistema.

O processo de Vinculação de Orientação será utilizado como **caso de validação ponta a ponta**.

---

# 2. Objetivo do Sprint

Construir uma primeira versão funcional do **Automation Development Environment — ADE**, capaz de:

1. criar ou abrir um projeto de automação;
2. carregar e editar um BPMN;
3. identificar elementos do processo que demandam automação;
4. associar esses elementos a capacidades existentes;
5. configurar integrações de entrada e saída;
6. configurar mensagens e correlações;
7. validar se a automação possui os elementos necessários para execução;
8. realizar o deployment do BPMN no Camunda 7;
9. utilizar serviços executados em containers Docker;
10. executar uma automação ponta a ponta;
11. integrar o processo a um sistema de domínio independente por REST.

O Sprint deverá também criar um **MVP independente do PPG Management System**, utilizado como sistema externo de domínio.

---

# 3. Princípio fundamental

O Codex não deverá simplesmente desenvolver a automação de Vinculação de Orientação diretamente no código.

O objetivo é criar um ambiente que permita ao usuário **construir e configurar essa automação através do ADE**.

A seguinte distinção é obrigatória:

```text
ERRADO

Codex implementa:
Vinculação de Orientação
        ↓
código específico
        ↓
Camunda
```

```text
CORRETO

Codex implementa:
Automation Development Environment
        ↓
usuário configura:
Vinculação de Orientação
        ↓
ADE produz/configura:
BPMN + integrações + bindings + deployment
        ↓
Camunda + serviços
```

A Vinculação de Orientação deve ser apenas o primeiro caso de uso.

---

# 4. Unidades independentes de implantação

O Sprint deve produzir pelo menos duas unidades independentes:

```text
1. propos26 Automation Platform

2. PPG Management System
```

Elas deverão possuir ciclos de execução independentes.

O usuário deve conseguir subir:

```text
PPG Management System
```

sem iniciar o propos26.

Da mesma forma, deve conseguir subir:

```text
propos26 Automation Platform
```

sem iniciar o PPG Management System.

A integração ocorrerá por API REST somente quando ambos estiverem disponíveis.

---

# 5. Containerização obrigatória

Todos os componentes executáveis deste Sprint deverão ser preparados para execução via Docker.

Não considerar Docker apenas como conveniência de desenvolvimento.

Docker faz parte do modelo de implantação do propos26.

---

# 6. Docker Compose independente do propos26

Criar ou ajustar um Docker Compose específico para a plataforma de automação.

Exemplo conceitual:

```text
propos26/
├── docker-compose.yml
├── .env.example
│
├── ADE
├── Camunda 7
├── GMS
├── CIR
└── automation services
```

O comando esperado deverá ser semelhante a:

```bash
docker compose up -d
```

e deverá iniciar a plataforma de automação.

Esse Docker Compose **não deverá iniciar automaticamente o PPG Management System**.

---

# 7. Docker Compose independente do PPG Management System

O PPG Management System deverá possuir seu próprio ambiente Docker.

Exemplo:

```text
ppg-management-system/
├── docker-compose.yml
├── .env.example
├── application/
└── database/
```

O sistema deverá poder ser iniciado com:

```bash
docker compose up -d
```

sem nenhuma dependência do Camunda, GMS, CIR ou ADE.

---

# 8. Comunicação entre ambientes

A comunicação entre propos26 e PPG Management deverá ser realizada exclusivamente por contrato explícito.

Inicialmente:

```text
REST/HTTP
```

O propos26 deverá receber por configuração algo semelhante a:

```text
PPG_MANAGEMENT_BASE_URL=http://...
```

Nunca utilizar:

- acesso direto ao banco de dados do PPG;
- dependência Maven entre os projetos;
- classes compartilhadas por acoplamento direto;
- conhecimento do Camunda dentro do PPG Management.

---

# 9. Automation Development Environment — ADE

Construir a primeira versão utilizável do ADE.

O ADE deverá ser uma aplicação web.

Neste Sprint ele deverá possuir no mínimo as seguintes áreas:

```text
Projects
BPMN
Automation
Integrations
Deployment
Execution
```

Não é necessário atingir ainda acabamento visual de produto final.

Priorizar funcionamento, clareza e separação arquitetural.

---

# 10. Gestão de projetos de automação

O ADE deverá permitir criar um:

```text
Automation Project
```

Exemplo:

```text
Name:
Vinculação de Orientação

Key:
vinculacao-orientacao

Version:
1.0
```

O projeto deverá agregar logicamente:

```text
BPMN model
Automation requirements
Capability bindings
Inbound integration definitions
Outbound integration definitions
Correlation definitions
Deployment configuration
```

---

# 11. Editor BPMN integrado

O ADE deverá incluir um editor BPMN embutido.

Utilizar preferencialmente:

```text
camunda-bpmn-js
```

ou outra solução validada no E01.

O editor deverá permitir pelo menos:

1. carregar um BPMN;
2. visualizar o BPMN;
3. editar o modelo;
4. salvar o BPMN;
5. exportar XML;
6. editar propriedades necessárias para Camunda 7.

Não é necessário reproduzir todas as funcionalidades do Camunda Modeler desktop.

O objetivo é permitir que o usuário permaneça dentro do ADE.

---

# 12. Processo de referência

Criar ou fornecer um BPMN inicial para:

```text
Vinculação de Orientação
```

Fluxo conceitual:

```text
Receber solicitação
        ↓
Verificar dados
        ↓
Dados completos?
     /      \
   NÃO      SIM
    ↓        ↓
Solicitar   Solicitar confirmação
dados       ao estudante
    ↓            ↓
Aguardar         Aguardar
resposta         confirmação
    ↓            ↓
retornar     Solicitar confirmação
à validação      ao coordenador
                     ↓
                 Aguardar
                 confirmação
                     ↓
              Registrar orientação
                     ↓
                    Fim
```

O BPMN deverá utilizar elementos compatíveis com Camunda 7.

---

# 13. Identificação de Automation Requirements

O ADE deverá analisar os elementos BPMN e apresentar os pontos que exigem configuração.

No mínimo:

## Service Task

Exemplo:

```text
Verificar dados
```

Gerar ou representar:

```text
Automation Requirement:
SERVICE_CAPABILITY
```

---

## Message Catch Event / Receive Task

Exemplo:

```text
Aguardar confirmação do estudante
```

Representar:

```text
Automation Requirements:
INBOUND_EVENT
MESSAGE_DEFINITION
CORRELATION_DEFINITION
```

---

## Send Task

Exemplo:

```text
Solicitar confirmação ao estudante
```

Representar:

```text
Automation Requirement:
OUTBOUND_COMMUNICATION
```

---

# 14. Capability Binding

O ADE deverá permitir associar uma atividade BPMN a uma capacidade disponível.

Exemplo:

```text
BPMN Element:
Registrar orientação

Capability:
REGISTER_ADVISORSHIP
```

Outro exemplo:

```text
BPMN Element:
Enviar solicitação de confirmação

Capability:
SEND_EMAIL
```

Não implementar ainda geração automática avançada de serviços.

Neste Sprint, trabalhar principalmente com capabilities existentes.

---

# 15. Capability Registry mínimo

Criar um registry mínimo, persistido ou configurável, contendo capacidades disponíveis.

Exemplo:

```text
SEND_EMAIL

VALIDATE_ADVISORSHIP_REQUEST

REGISTER_ADVISORSHIP
```

Cada capability deverá ter pelo menos:

```text
id
name
type
provider
interfaceType
status
```

Exemplo:

```json
{
  "id": "REGISTER_ADVISORSHIP",
  "name": "Register Advisorship",
  "type": "DOMAIN_SERVICE",
  "provider": "ppg-management-system",
  "interfaceType": "REST",
  "status": "AVAILABLE"
}
```

---

# 16. Configuração de integrações

O ADE deverá permitir configurar integrações sem alterar código-fonte do CIR ou GMS para cada processo.

O objetivo do E02 é demonstrar essa direção.

Para mensagens recebidas por e-mail, permitir representar algo semelhante a:

```text
Channel:
EMAIL

Provider:
GMS

External Event:
CONFIRMACAO_ESTUDANTE

Camunda Message:
Message_ConfirmacaoEstudante

Correlation Field:
requestId
```

---

# 17. GMS

Reutilizar o GMS existente.

Evitar reescrita.

Adaptar apenas se necessário para suportar configuração mais genérica.

O GMS deverá continuar responsável por:

```text
email access
polling
message normalization
bindings
message lifecycle
```

O GMS não deverá conhecer o significado de eventos específicos do processo.

---

# 18. CIR

Reutilizar o CIR existente.

O E02 deverá evoluí-lo, se necessário, para reduzir configurações hard-coded.

O objetivo é que o CIR possa receber uma definição semelhante a:

```text
External Event:
CONFIRMACAO_ESTUDANTE

Action:
CORRELATE_MESSAGE

Message:
Message_ConfirmacaoEstudante

Correlation Variable:
requestId
```

Outro exemplo:

```text
External Event:
VINCULACAO_SOLICITADA

Action:
START_PROCESS

Process Definition Key:
vinculacao_orientacao

Business Key:
requestId
```

Essas regras deverão progressivamente ser produzidas pelo ADE.

---

# 19. Deployment pelo ADE

O ADE deverá possuir uma primeira versão da operação:

```text
DEPLOY AUTOMATION
```

No E02, o deployment deverá realizar pelo menos:

1. validar o BPMN;
2. validar Automation Requirements;
3. validar Capability Bindings;
4. validar configurações de integração;
5. validar definições de correlação;
6. verificar disponibilidade básica dos serviços;
7. fazer deployment do BPMN no Camunda 7;
8. disponibilizar/aplicar as configurações necessárias ao CIR;
9. disponibilizar/aplicar configurações necessárias ao GMS, quando aplicável;
10. registrar o estado do deployment.

O sistema deverá exibir claramente falhas antes de indicar sucesso.

---

# 20. Estado de deployment

Utilizar estados conceituais como:

```text
DRAFT
VALID
DEPLOYING
READY
FAILED
```

Exemplo:

```text
Automation:
Vinculação de Orientação

Version:
1.0

Status:
READY
```

---

# 21. PPG Management System — MVP

Criar um sistema de domínio independente denominado:

```text
PPG Management System
```

Sugestão de projeto:

```text
ppg-management-service
```

Tecnologia preferencial:

```text
Spring Boot
REST
JPA
relational database
Docker
```

Esse sistema deverá permanecer completamente independente do Camunda.

---

# 22. Modelo mínimo de domínio

Implementar pelo menos:

## Student

```text
id
registration
name
email
status
```

## Professor

```text
id
name
email
status
```

## Advisorship

```text
id
studentId
advisorId
title
researchArea
startDate
status
```

Status inicial relevante:

```text
IN_PROGRESS
```

---

# 23. API mínima do PPG Management

Implementar:

```http
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

Adicionar:

```http
GET /api/health
```

---

# 24. Registro da orientação

O processo deverá concluir chamando:

```http
POST /api/advisorships
```

Exemplo:

```json
{
  "studentId": 17,
  "advisorId": 4,
  "title": "Process Mining for Business Process Automation",
  "researchArea": "Information Systems",
  "startDate": "2026-08-23",
  "status": "IN_PROGRESS"
}
```

O PPG Management deverá persistir a informação.

---

# 25. Interface mínima do PPG Management

Criar uma interface web simples.

Ela deverá permitir pelo menos visualizar:

```text
Students
Professors
Advisorships
```

O objetivo é tornar visível o resultado da automação.

Exemplo:

```text
Advisorships

Student: Maria Silva
Advisor: Prof. João Souza
Title: Process Mining for ...
Research Area: Information Systems
Status: IN_PROGRESS
```

Não criar neste Sprint um sistema acadêmico completo.

---

# 26. Independência do PPG Management

É proibido ao PPG Management:

- conhecer Camunda;
- conhecer CIR;
- conhecer GMS;
- consultar o banco do Camunda;
- depender de classes internas do propos26.

Para o PPG Management:

```text
POST /api/advisorships
```

é apenas uma chamada REST de um cliente externo.

---

# 27. Automation Service para registro

A integração entre Camunda e PPG Management deverá ocorrer através de um componente de automação.

Exemplo:

```text
Camunda Service Task
        ↓
REGISTER_ADVISORSHIP
        ↓
Automation Service / Worker
        ↓
REST
        ↓
PPG Management
```

Evitar colocar lógica de integração diretamente no modelo BPMN.

---

# 28. Configuração do endpoint externo

A URL do PPG Management deverá ser externa ao código.

Exemplo:

```text
PPG_MANAGEMENT_BASE_URL
```

Desenvolvimento:

```text
http://host.docker.internal:8090
```

ou outra estratégia adequada ao ambiente Docker.

Não assumir um endereço fixo.

---

# 29. Pipeline de implantação

O E02 deverá implementar a primeira versão operacional do pipeline:

```text
Automation Project
        ↓
Validate
        ↓
Resolve Capabilities
        ↓
Validate Integrations
        ↓
Validate External Services
        ↓
Deploy BPMN
        ↓
Apply CIR/GMS Configuration
        ↓
Automation READY
```

Neste Sprint não é obrigatório gerar automaticamente novos serviços.

---

# 30. Pipeline Docker

O ambiente deverá estar preparado para uma evolução futura para:

```text
Generate Service
      ↓
Maven Build
      ↓
Tests
      ↓
Docker Image
      ↓
Container
      ↓
Register Capability
      ↓
Deploy Automation
```

O E02 deverá evitar decisões arquiteturais que dificultem essa evolução.

---

# 31. Execução ponta a ponta

A prova de aceitação principal deverá executar:

```text
1. Criar/abrir Automation Project no ADE

2. Abrir BPMN de Vinculação de Orientação

3. Configurar capabilities

4. Configurar entrada inicial

5. Configurar mensagens

6. Configurar correlation key

7. Configurar REGISTER_ADVISORSHIP

8. Fazer Deploy Automation

9. Enviar solicitação inicial

10. GMS recebe

11. CIR inicia processo

12. Camunda executa

13. Processo solicita complementação ou confirmação

14. Respostas chegam por e-mail

15. GMS recebe

16. CIR correlaciona

17. Camunda continua

18. Estudante confirma

19. Coordenador confirma

20. Automation Service chama PPG Management

21. PPG Management cria Advisorship

22. Processo termina

23. Interface do PPG mostra a nova orientação como IN_PROGRESS
```

---

# 32. Requisito de genericidade

É proibido implementar no ADE telas ou código cujo significado seja especificamente:

```text
Vinculação de Orientação
```

O ADE deve trabalhar com conceitos genéricos:

```text
Process
BPMN Element
Automation Requirement
Capability
Inbound Event
Outbound Communication
Correlation
Service Binding
Deployment
```

O processo de referência apenas instancia esses conceitos.

---

# 33. Segundo teste recomendado

Se houver tempo no Sprint, criar um processo extremamente simples adicional, como:

```text
Solicitação de Declaração
```

O objetivo é verificar se uma nova automação pode ser configurada sem alterar o código-fonte do ADE.

Esse segundo processo não precisa possuir todas as integrações do primeiro.

---

# 34. Estrutura esperada de execução

A arquitetura operacional deverá ser semelhante a:

```text
+--------------------------------------------------+
|            propos26 Automation Platform          |
|                                                  |
|  +-------+    +---------+    +---------------+   |
|  |  ADE  |    | Camunda |    | Automation    |   |
|  |       |    |    7    |    | Services      |   |
|  +-------+    +---------+    +---------------+   |
|                                                  |
|  +-------+                  +----------------+   |
|  |  GMS  | ---------------->|      CIR       |   |
|  +-------+                  +----------------+   |
+--------------------------------------------------+

                         REST
                          |
                          v

+--------------------------------------------------+
|              PPG Management System               |
|                                                  |
|   Students                                       |
|   Professors                                     |
|   Advisorships                                   |
|   Database                                       |
+--------------------------------------------------+
```

Os dois blocos devem ser implantáveis independentemente.

---

# 35. Docker requirements

## propos26 Automation Platform

Deverá possuir documentação para:

```bash
docker compose build
docker compose up -d
docker compose down
docker compose logs
```

## PPG Management System

Deverá possuir documentação equivalente:

```bash
docker compose build
docker compose up -d
docker compose down
docker compose logs
```

---

# 36. Health checks

Cada componente principal deverá possuir, quando aplicável, um endpoint ou mecanismo de health check.

Exemplos:

```text
ADE
GMS
CIR
PPG Management
Automation Services
Camunda
```

O Docker Compose deverá utilizar health checks quando tecnicamente adequado.

---

# 37. Configuração externa

Utilizar:

```text
.env
.env.example
environment variables
application properties
```

Nunca adicionar ao Git:

```text
passwords
tokens
API keys
email credentials
database passwords reais
```

---

# 38. Artefatos obrigatórios

Criar ou atualizar:

```text
docs/e02/overview.md

docs/e02/ade.md

docs/e02/deployment.md

docs/e02/docker.md

docs/e02/ppg-management.md

docs/e02/reference-automation.md

docs/e02/e2e-test.md
```

---

# 39. Documentação de APIs

Documentar as APIs novas ou alteradas.

Preferencialmente utilizar OpenAPI/Swagger para:

```text
PPG Management
ADE backend
CIR configuration API
GMS configuration API
```

quando existirem.

---

# 40. Critérios de aceitação do ADE

O ADE será considerado minimamente funcional quando for possível:

1. acessar via navegador;
2. criar ou abrir Automation Project;
3. carregar BPMN;
4. editar BPMN;
5. salvar BPMN;
6. visualizar elementos que requerem automação;
7. associar capabilities;
8. configurar pelo menos uma entrada por mensagem;
9. configurar correlação;
10. validar o projeto;
11. fazer deployment no Camunda 7;
12. visualizar estado do deployment.

---

# 41. Critérios de aceitação do PPG Management

O sistema será considerado funcional quando:

1. iniciar independentemente via Docker Compose;
2. possuir banco persistente;
3. permitir cadastrar estudantes;
4. permitir cadastrar professores;
5. permitir cadastrar orientações via REST;
6. permitir consultar orientações;
7. disponibilizar interface web mínima;
8. mostrar orientação criada pelo processo;
9. não depender de Camunda, GMS, CIR ou ADE.

---

# 42. Critérios de aceitação ponta a ponta

O Sprint será considerado concluído quando:

1. propos26 subir via Docker;
2. PPG Management subir independentemente via Docker;
3. ambos puderem funcionar em processos separados;
4. ADE puder configurar a automação de referência;
5. BPMN puder ser implantado no Camunda 7;
6. GMS puder receber a solicitação;
7. CIR puder iniciar a instância;
8. mensagens posteriores puderem ser correlacionadas;
9. o processo puder chamar uma capability de domínio;
10. essa capability puder realizar chamada REST ao PPG Management;
11. uma orientação puder ser persistida;
12. a orientação aparecer como `IN_PROGRESS`;
13. o processo terminar com sucesso;
14. não houver código específico de Vinculação de Orientação no núcleo do ADE;
15. GMS e CIR continuarem reutilizáveis.

---

# 43. Procedimento de trabalho para o Codex

## Fase A — Revisão do E01

Ler toda a documentação produzida no Sprint E01.

Verificar ADRs, arquitetura alvo, gaps e decisões pendentes.

---

## Fase B — Validar sistema atual

Executar builds e testes existentes.

Não iniciar grandes alterações antes de garantir que o estado atual é conhecido.

---

## Fase C — ADE MVP

Criar a aplicação web do ADE.

Priorizar:

```text
Projects
BPMN Editor
Automation Requirements
Capability Binding
Integration Configuration
Deployment
```

---

## Fase D — PPG Management MVP

Criar o sistema de domínio independente.

Implementar:

```text
Student
Professor
Advisorship
REST API
Database
Basic UI
Docker
```

---

## Fase E — Integrações

Adaptar GMS e CIR apenas quando necessário para suportar configuração genérica.

---

## Fase F — Deployment

Implementar a primeira versão de:

```text
Deploy Automation
```

---

## Fase G — Docker

Garantir execução independente de:

```text
propos26
```

e:

```text
ppg-management-system
```

---

## Fase H — End-to-End

Executar o caso completo da Vinculação de Orientação.

Não considerar o Sprint concluído apenas porque os módulos compilam.

A execução ponta a ponta é obrigatória.

---

## Fase I — Relatório

Criar:

```text
docs/sprints/E02-report.md
```

contendo:

- estado inicial;
- decisões tomadas;
- arquitetura implementada;
- funcionalidades do ADE;
- funcionalidades do PPG Management;
- alterações em GMS;
- alterações em CIR;
- componentes Docker;
- APIs criadas;
- problemas encontrados;
- limitações;
- dívida técnica;
- resultados do teste E2E;
- recomendações para E03.

---

# 44. Restrições

Não:

- migrar para Camunda 8;
- substituir GMS sem necessidade;
- substituir CIR sem necessidade;
- acoplar PPG Management ao Camunda;
- implementar regras de domínio no GMS;
- implementar regras de domínio no CIR;
- criar código hard-coded para cada processo dentro do ADE;
- criar um sistema PPG completo;
- introduzir Kubernetes neste Sprint;
- criar geração de código por IA completa neste Sprint;
- criar abstrações excessivas sem necessidade.

---

# 45. Resultado esperado

Ao final do Sprint, deve ser possível demonstrar:

```text
1. PPG Management é iniciado via Docker.

2. propos26 é iniciado separadamente via Docker.

3. O usuário abre o ADE.

4. Cria ou abre o projeto:
   Vinculação de Orientação.

5. Visualiza e edita o BPMN.

6. Configura as necessidades de automação.

7. Configura eventos e correlações.

8. Associa capacidades existentes.

9. Realiza Deploy Automation.

10. O processo é implantado no Camunda 7.

11. Uma solicitação real inicia uma instância.

12. As mensagens seguintes são correlacionadas.

13. O processo chama o PPG Management via REST.

14. O PPG Management registra a orientação.

15. A interface do PPG mostra:
    Advisorship = IN_PROGRESS.
```

Essa demonstração deverá ser possível sem editar manualmente o código-fonte do ADE para adaptar o ambiente especificamente ao processo de Vinculação de Orientação.

---

# 46. Diretriz final

O E02 deverá marcar a transição do propos26 de uma arquitetura conceitual para uma **plataforma mínima operacional de automação de processos**.

O objetivo final permanece:

> permitir que um usuário com conhecimento limitado de programação modele um processo, configure suas necessidades de automação e coloque esse processo em operação através de serviços reutilizáveis e componentes implantados via Docker.

A implementação deste Sprint deve privilegiar uma experiência de automação orientada pelo processo e não uma experiência de desenvolvimento manual de microsserviços.
