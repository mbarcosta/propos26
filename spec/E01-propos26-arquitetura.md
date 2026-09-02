# E01 — Sprint 1 — Arquitetura do Ecossistema propos26

## 1. Visão do projeto

O **propos26** tem como objetivo criar um ecossistema para **facilitar a automação de novos processos de negócio**, reduzindo a dependência de conhecimento técnico especializado.

O usuário-alvo inicial pode ser, por exemplo, um estagiário com conhecimento limitado de desenvolvimento de software e de automação de processos.

A expectativa é que esse usuário consiga, de forma assistida:

1. modelar um processo em BPMN;
2. submeter ou editar esse modelo dentro do ecossistema;
3. identificar quais atividades do BPMN precisam de automação;
4. reutilizar serviços existentes sempre que possível;
5. criar, de forma assistida, novos componentes de software quando necessário;
6. configurar entradas e saídas do processo;
7. configurar mensagens e correlações;
8. implantar o processo no Camunda;
9. executar e testar o processo;
10. monitorar a execução.

O propos26 deve evoluir, portanto, para um **ambiente de desenvolvimento orientado a processos**, e não apenas para um conjunto de microsserviços integrados ao Camunda.

---

## 2. Princípio arquitetural central

A arquitetura deve ser guiada pelo seguinte ciclo:

```text
MODELAR
   ↓
ANALISAR NECESSIDADES DE AUTOMAÇÃO
   ↓
LOCALIZAR CAPACIDADES EXISTENTES
   ↓
CRIAR/CONFIGURAR CAPACIDADES AUSENTES
   ↓
VINCULAR CAPACIDADES AO BPMN
   ↓
CONFIGURAR EVENTOS, MENSAGENS E CORRELAÇÕES
   ↓
IMPLANTAR
   ↓
EXECUTAR
   ↓
TESTAR
   ↓
MONITORAR
```

Todo componente arquitetural deve existir para apoiar alguma etapa desse ciclo.

---

# 3. Objetivo do Sprint E01

O objetivo deste sprint é transformar o estado atual do `propos26` em uma **arquitetura-base de uma plataforma assistida de automação de processos**.

O Sprint deve:

- analisar o repositório existente;
- preservar e incorporar GMS e CIR;
- utilizar Camunda 7 como motor de processos;
- definir um ambiente central de desenvolvimento e automação;
- definir a estratégia para incorporar modelagem BPMN ao ambiente;
- definir como atividades BPMN serão associadas a capacidades de software;
- criar a base conceitual para geração assistida de novos componentes;
- definir contratos entre os componentes;
- preparar a arquitetura para implantação, testes e monitoramento de processos.

O Sprint **não deve ainda implementar toda a plataforma**.

O resultado esperado é uma fundação arquitetural coerente sobre a qual os próximos sprints poderão evoluir.

---

# 4. Primeira atividade obrigatória: compreender o estado atual

Antes de realizar qualquer refatoração relevante:

1. examinar completamente o repositório `propos26`;
2. identificar os módulos Maven existentes;
3. identificar aplicações Spring Boot;
4. identificar Dockerfiles e Docker Compose existentes;
5. identificar arquivos de configuração;
6. localizar GMS;
7. localizar CIR;
8. identificar a integração atual com Camunda 7;
9. identificar workers e serviços já existentes;
10. identificar endpoints REST;
11. identificar DTOs e contratos;
12. identificar mecanismos atuais de configuração;
13. identificar mecanismos atuais de correlação de mensagens;
14. identificar dependências entre os módulos;
15. identificar acoplamentos específicos a processos já implementados.

Criar:

```text
docs/architecture/current-state.md
```

Esse documento deve representar fielmente o sistema encontrado antes das mudanças.

---

# 5. Componentes arquiteturais principais

A arquitetura deverá ser organizada inicialmente nos seguintes componentes.

---

## 5.1. Automation Development Environment — ADE

Criar conceitualmente um componente central denominado:

**Automation Development Environment — ADE**

Nome de projeto sugerido:

```text
automation-development-environment
```

Esse ambiente substitui a ideia de um simples "Service Development Environment".

O ADE será o principal ponto de interação do usuário com o ecossistema propos26.

Seu objetivo é tornar a construção de automações orientada pelo BPMN.

O usuário deverá futuramente conseguir realizar dentro dele:

- criar projetos de automação;
- criar ou importar BPMN;
- editar BPMN;
- validar BPMN;
- identificar atividades que exigem implementação;
- consultar capacidades existentes;
- associar tarefas BPMN a serviços existentes;
- criar serviços sob demanda;
- configurar External Task Workers;
- configurar integrações REST;
- configurar entrada por e-mail;
- configurar mensagens;
- configurar correlações;
- configurar variáveis de processo;
- implantar o BPMN;
- executar instâncias de teste;
- visualizar logs;
- visualizar o estado das instâncias.

O ADE deve ser considerado a **IDE de automação de processos do propos26**.

---

# 6. Modelagem BPMN integrada

Uma meta arquitetural importante é evitar que o usuário precise trabalhar continuamente entre várias ferramentas.

O ADE deve possuir um **editor BPMN integrado**.

Para Camunda 7, avaliar prioritariamente o uso de:

```text
camunda-bpmn-js
```

ou, se necessário:

```text
bpmn-js
```

O objetivo não é incorporar literalmente a aplicação desktop Camunda Modeler.

O objetivo é incorporar no ADE a **experiência de edição BPMN compatível com Camunda 7**.

O editor deverá futuramente permitir:

- abrir BPMN;
- criar BPMN;
- modificar BPMN;
- editar propriedades Camunda;
- definir tipos de tarefas;
- configurar External Task topics;
- configurar mensagens;
- configurar eventos;
- configurar gateways;
- salvar BPMN;
- validar BPMN;
- implantar BPMN no Camunda 7.

Inicialmente, criar um estudo técnico:

```text
docs/architecture/embedded-bpmn-modeler.md
```

O estudo deverá avaliar:

```text
camunda-bpmn-js
bpmn-js
bpmn-js-properties-panel
bpmn-moddle
```

e indicar a solução recomendada para Camunda 7.

---

# 7. Camunda 7

O Camunda 7 será o **motor de execução e orquestração**.

Responsabilidades:

- executar BPMN;
- manter estado das instâncias;
- controlar tokens;
- executar gateways;
- criar tarefas;
- receber mensagens;
- correlacionar mensagens;
- gerenciar variáveis;
- acionar External Tasks;
- manter histórico;
- disponibilizar APIs REST.

O Camunda não deve concentrar regras de negócio complexas.

Princípio:

```text
Camunda = orquestração
```

---

# 8. GMS — Email Gateway Service

O GMS existente deverá ser preservado.

Responsabilidades:

- conexão com servidores de e-mail;
- polling;
- leitura de mensagens;
- normalização;
- aplicação de bindings;
- identificação de mensagens;
- movimentação de mensagens processadas;
- disponibilização das mensagens através de API.

O GMS deve ser **agnóstico em relação ao processo BPMN**.

Ele não deve saber o que significa:

```text
VINCULACAO_RECEBIDA
CONFIRMACAO_ESTUDANTE
CONFIRMACAO_COORDENADOR
```

Essas interpretações pertencem a níveis superiores da arquitetura.

Princípio:

```text
GMS = gateway de comunicação
```

---

# 9. CIR — Camunda Inbound Router

O CIR existente deverá ser preservado e progressivamente generalizado.

Responsabilidades:

- receber eventos externos normalizados;
- classificar eventos;
- extrair informações;
- associar eventos a configurações de automação;
- iniciar processos;
- correlacionar mensagens com instâncias existentes;
- enviar variáveis ao Camunda.

O objetivo futuro é evitar que seja necessário programar manualmente o CIR a cada novo processo.

As regras de roteamento devem progressivamente ser configuráveis pelo ADE.

Exemplo:

```text
Evento externo:
CONFIRMACAO_ESTUDANTE

Process Definition:
vinculacao_orientacao

Message:
Message_ConfirmacaoEstudante

Correlation Key:
requestId
```

Esses dados devem poder ser gerenciados por configuração.

Princípio:

```text
CIR = roteamento e correlação de eventos
```

---

# 10. Automation Services

Criar o conceito de **Automation Service**.

São componentes executáveis usados pelos processos.

Exemplos:

```text
VALIDATE_REQUEST
SEND_EMAIL
QUERY_STUDENT
REGISTER_ADVISORSHIP
GENERATE_DOCUMENT
VALIDATE_CPF
QUERY_ACADEMIC_SYSTEM
```

Esses componentes poderão assumir diferentes formas:

```text
EXTERNAL_TASK_WORKER
REST_SERVICE
INTEGRATION_ADAPTER
TRANSFORMER
RULE_SERVICE
AI_SERVICE
```

O BPMN deve utilizar a capacidade fornecida pelo serviço, não conhecer sua implementação interna.

---

# 11. Automation Capability Registry

Criar conceitualmente um **Automation Capability Registry**.

O registry deverá informar quais capacidades já existem no ecossistema.

Exemplo:

```json
{
  "id": "SEND_EMAIL",
  "name": "Send Email",
  "type": "INTEGRATION",
  "provider": "GMS",
  "interface": "REST",
  "status": "AVAILABLE"
}
```

Outro exemplo:

```json
{
  "id": "VALIDATE_CPF",
  "name": "Validate CPF",
  "type": "BUSINESS_SERVICE",
  "provider": "cpf-service",
  "interface": "REST",
  "status": "AVAILABLE"
}
```

O ADE deverá consultar esse catálogo antes de propor a criação de novos serviços.

O fluxo futuro deverá ser:

```text
Tarefa BPMN
    ↓
Necessidade de automação
    ↓
Existe capability compatível?
    ↓
SIM --------------------→ configurar utilização
    ↓ NÃO
criar novo serviço
```

---

# 12. Geração assistida de componentes

O ADE deverá ser projetado para futuramente realizar **scaffolding assistido de componentes de software**.

Exemplo:

O usuário seleciona a tarefa BPMN:

```text
Verificar dados da solicitação
```

E informa:

```text
Entrada:
- orientador
- estudante
- tema
- área

Saída:
- complete : boolean
- missingFields : list
```

O ADE poderá gerar um skeleton:

```text
validate-request-service
```

ou um:

```text
External Task Worker
```

com:

- projeto Maven;
- Spring Boot;
- DTOs;
- endpoint ou worker;
- configuração;
- Dockerfile;
- testes iniciais;
- documentação;
- contrato de integração.

Este Sprint deve **definir a arquitetura dessa capacidade**, mas não precisa implementar geração completa.

Criar:

```text
docs/architecture/service-generation.md
```

---

# 13. Processo de automação orientado pelo BPMN

O ADE deverá futuramente analisar o BPMN e identificar elementos que exigem configuração ou implementação.

Exemplo:

```text
Receive Task
Message Catch Event
Service Task
Send Task
External Task
```

Para cada elemento, o ambiente poderá identificar requisitos.

Exemplo:

```text
Service Task: "Verificar dados"

Automation Requirement:
SERVICE_IMPLEMENTATION
```

Exemplo:

```text
Intermediate Message Catch Event:
"Aguardar confirmação do estudante"

Automation Requirements:
INBOUND_EVENT
CORRELATION_RULE
MESSAGE_DEFINITION
```

Exemplo:

```text
Send Task:
"Solicitar confirmação"

Automation Requirement:
OUTBOUND_COMMUNICATION
```

Introduzir formalmente o conceito:

```text
AutomationRequirement
```

---

# 14. Modelo conceitual inicial do ADE

Preparar um modelo contendo pelo menos:

```text
AutomationProject

ProcessModel

BpmnElement

AutomationRequirement

AutomationCapability

ServiceDefinition

ServiceImplementation

IntegrationBinding

InboundEventDefinition

OutboundMessageDefinition

CorrelationDefinition

Deployment

ExecutionEnvironment
```

O objetivo é permitir rastrear:

```text
Elemento BPMN
      ↓
Necessidade de automação
      ↓
Capability
      ↓
Implementação
      ↓
Deployment
```

---

# 15. Domain Systems

Sistemas que mantêm dados permanentes do domínio devem permanecer fora do Camunda.

Criar conceitualmente:

```text
ppg-management-service
```

Responsabilidades futuras:

- estudantes;
- professores;
- orientações;
- dissertações;
- programas;
- cursos;
- áreas;
- linhas de pesquisa.

Princípio:

```text
Camunda = estado do processo

PPG Management = estado do domínio
```

Não utilizar o banco de dados do Camunda como banco de dados do sistema acadêmico.

---

# 16. Caso de referência — Vinculação de Orientação

Utilizar como principal caso arquitetural:

```text
Vinculação de Orientação
```

O processo inicia quando uma solicitação é recebida.

Dados esperados:

- orientador;
- estudante;
- tema;
- área.

Fluxo:

```text
Orientador
    ↓
Email
    ↓
GMS
    ↓
CIR
    ↓
Camunda
    ↓
Verificar dados
```

Se faltarem dados:

```text
Camunda
   ↓
SEND_EMAIL
   ↓
Orientador
   ↓
resposta
   ↓
GMS
   ↓
CIR
   ↓
correlation
   ↓
Camunda
```

Quando os dados estiverem completos:

```text
Camunda
   ↓
solicita confirmação
   ↓
Estudante
   ↓
resposta
   ↓
GMS
   ↓
CIR
   ↓
Camunda
```

Depois:

```text
Camunda
   ↓
Coordenador
   ↓
confirmação
   ↓
CIR
   ↓
Camunda
```

Finalmente:

```text
Camunda
   ↓
REGISTER_ADVISORSHIP
   ↓
PPG Management System
   ↓
Dissertation = IN_PROGRESS
```

---

# 17. Requisito fundamental do caso de referência

O usuário que automatiza esse processo **não deve precisar programar manualmente cada integração**.

O objetivo futuro do ADE é permitir que o usuário configure algo semelhante a:

```text
Tarefa BPMN:
Solicitar confirmação do estudante

Capability:
SEND_EMAIL

Recipient:
${student.email}

Template:
advisor_confirmation_student

Expected Response Event:
CONFIRMACAO_ESTUDANTE

Correlation Field:
requestId
```

A partir disso, o ADE deverá futuramente gerar as configurações necessárias para:

```text
Camunda
GMS
CIR
```

Esse é um requisito arquitetural central.

---

# 18. Configuração sobre código

Sempre que possível, novas automações deverão exigir:

```text
configuração
```

antes de exigir:

```text
programação
```

Exemplos que devem ser progressivamente configuráveis:

- bindings de e-mail;
- eventos de entrada;
- nomes de mensagens BPMN;
- correlation keys;
- destinatários;
- templates;
- External Task topics;
- endpoints REST;
- variáveis de entrada;
- variáveis de saída;
- credenciais externas;
- parâmetros de deployment.

---

# 19. Arquitetura alvo

Propor inicialmente uma arquitetura semelhante a:

```text
                       +--------------------------------------+
                       | Automation Development Environment   |
                       |                ADE                   |
                       |--------------------------------------|
                       | Embedded BPMN Modeler                |
                       | Requirement Analyzer                 |
                       | Capability Registry                  |
                       | Service Generator                    |
                       | Integration Configurator             |
                       | Deployment Manager                   |
                       | Testing / Execution Console          |
                       +------------------+-------------------+
                                          |
            +-----------------------------+----------------------------+
            |                             |                            |
            v                             v                            v
    +---------------+              +---------------+           +---------------+
    |      GMS      |              |      CIR      |           |   Camunda 7   |
    | Email Gateway |------------->| Event Router  |---------->| Process Engine|
    +---------------+              +---------------+           +-------+-------+
                                                                         |
                                                                         |
                                      +----------------------------------+----------------+
                                      |                                                   |
                                      v                                                   v
                           +-----------------------+                         +----------------------+
                           | Automation Services   |                         | Domain Systems       |
                           |-----------------------|                         |----------------------|
                           | Workers               |                         | PPG Management       |
                           | REST Services         |                         | Other Systems        |
                           | Integrations          |                         |                      |
                           +-----------------------+                         +----------------------+
```

O Codex deverá refinar esse desenho depois de analisar o repositório existente.

---

# 20. Organização lógica proposta

Avaliar uma estrutura semelhante a:

```text
propos26/
│
├── platform/
│   ├── automation-development-environment/
│   └── capability-registry/
│
├── process-engine/
│   └── camunda7/
│
├── integration/
│   ├── email-gateway-service/
│   └── camunda-inbound-router/
│
├── automation-services/
│   ├── workers/
│   └── services/
│
├── domain-systems/
│   └── ppg-management-service/
│
├── processes/
│
├── infrastructure/
│
└── docs/
```

Não reorganizar fisicamente o código apenas por estética.

Primeiro avaliar impacto.

---

# 21. Contratos

Documentar contratos para pelo menos:

## GMS → CIR

```json
{
  "messageId": "...",
  "channel": "EMAIL",
  "from": "...",
  "to": ["..."],
  "subject": "...",
  "body": "...",
  "receivedAt": "...",
  "bindingId": "..."
}
```

Preservar DTOs existentes sempre que adequados.

---

## CIR → Camunda

Operações fundamentais:

```text
START_PROCESS
CORRELATE_MESSAGE
```

---

## Camunda → Automation Services

Priorizar:

```text
External Tasks
```

quando a atividade for naturalmente um worker desacoplado.

Permitir REST quando arquiteturalmente adequado.

---

## Automation Services → Domain Systems

Utilizar APIs públicas dos sistemas de domínio.

Não acessar diretamente bancos pertencentes a outros componentes.

---

# 22. Correlação

Definir mecanismo genérico de correlação.

Exemplos:

```text
businessKey
requestId
correlationId
```

O ADE deverá futuramente permitir configurar:

```text
External Event
      ↓
Message BPMN
      ↓
Correlation Variable
```

Exemplo:

```text
CONFIRMACAO_ESTUDANTE
      ↓
Message_ConfirmacaoEstudante
      ↓
requestId
```

---

# 23. Deployment

O ADE deverá futuramente possuir uma função:

```text
Deploy Automation
```

Essa função poderá envolver:

1. validar BPMN;
2. salvar versão;
3. implantar BPMN no Camunda;
4. publicar configurações do CIR;
5. publicar bindings necessários no GMS;
6. implantar workers ou serviços;
7. registrar capabilities;
8. validar endpoints;
9. produzir relatório do deployment.

Neste Sprint, definir o modelo arquitetural dessa operação.

---

# 24. Teste assistido

Prever no ADE uma futura área:

```text
Test Automation
```

Ela deverá permitir:

- iniciar instância;
- fornecer variáveis iniciais;
- acompanhar passos;
- visualizar variáveis;
- visualizar mensagens aguardadas;
- visualizar External Tasks;
- consultar falhas;
- observar eventos recebidos;
- observar correlações;
- reiniciar um teste.

Não implementar toda essa funcionalidade no E01.

---

# 25. Infraestrutura

Preparar uma estratégia Docker Compose para execução local contendo progressivamente:

```text
Camunda 7
GMS
CIR
ADE
PPG Management Service
Automation Services
```

Credenciais nunca devem ser armazenadas diretamente no repositório.

Usar:

```text
.env
.env.example
environment variables
external configuration
```

---

# 26. Artefatos obrigatórios

Criar:

```text
docs/architecture/current-state.md

docs/architecture/target-architecture.md

docs/architecture/components.md

docs/architecture/integration-contracts.md

docs/architecture/embedded-bpmn-modeler.md

docs/architecture/automation-development-environment.md

docs/architecture/automation-capabilities.md

docs/architecture/service-generation.md

docs/architecture/deployment-model.md

docs/architecture/reference-process.md
```

---

# 27. ADRs

Criar:

```text
docs/architecture/adr/
```

ADRs iniciais:

```text
ADR-001-use-camunda7-as-process-engine.md

ADR-002-separate-process-state-and-domain-state.md

ADR-003-use-gms-as-email-gateway.md

ADR-004-use-cir-as-inbound-event-router.md

ADR-005-create-automation-development-environment.md

ADR-006-embed-bpmn-modeling-in-ade.md

ADR-007-introduce-automation-capabilities.md

ADR-008-prefer-configuration-over-process-specific-code.md
```

Estado inicial:

```text
PROPOSED
```

quando a decisão ainda depender de validação técnica.

---

# 28. Prova de conceito do editor BPMN

Se tecnicamente possível dentro do escopo do Sprint, criar uma pequena prova de conceito no ADE contendo:

```text
BPMN Editor
```

A prova deverá:

1. carregar um BPMN;
2. renderizar o BPMN;
3. permitir editar o diagrama;
4. acessar propriedades Camunda 7;
5. exportar o XML BPMN resultante.

Não é necessário implementar neste momento:

- gerenciamento completo de projetos;
- deployment pelo editor;
- geração de serviços;
- colaboração;
- versionamento avançado.

O objetivo é apenas reduzir o maior risco arquitetural:

**confirmar que a modelagem BPMN pode ser incorporada ao ADE.**

---

# 29. Gap Analysis

Depois de definir a arquitetura alvo, comparar:

```text
CURRENT STATE
     ↓
TARGET STATE
```

Classificar gaps:

```text
CRITICAL
REQUIRED
DESIRABLE
FUTURE
```

Atenção especial para:

- código específico de processos dentro do CIR;
- código específico dentro do GMS;
- configuração hard-coded;
- ausência de contracts;
- ausência de correlation metadata;
- ausência de registry;
- ausência de deployment model;
- acoplamento entre processos e serviços.

---

# 30. Alterações permitidas

São permitidas neste Sprint:

- documentação arquitetural;
- criação do skeleton do ADE;
- prova de conceito do BPMN editor;
- criação de interfaces;
- criação de DTOs compartilhados quando realmente necessários;
- generalização pontual do CIR;
- generalização pontual do GMS;
- criação de configuração externa;
- criação do skeleton do Capability Registry;
- criação do skeleton do PPG Management Service;
- criação de infraestrutura Docker.

Evitar:

- reescrever GMS;
- reescrever CIR;
- criar uma plataforma excessivamente genérica;
- implementar todo o PPG Management;
- gerar automaticamente serviços completos;
- implementar toda a interface do ADE;
- migrar para Camunda 8;
- alterar tecnologias sem necessidade.

---

# 31. Critérios de aceitação

O Sprint será considerado concluído quando:

1. o repositório atual tiver sido analisado;
2. existir documentação do estado atual;
3. existir arquitetura alvo;
4. o ADE estiver definido como ponto central do ecossistema;
5. existir estratégia clara para modelagem BPMN integrada;
6. tiver sido avaliado `camunda-bpmn-js`/`bpmn-js`;
7. GMS tiver responsabilidade claramente delimitada;
8. CIR tiver responsabilidade claramente delimitada;
9. Camunda 7 estiver definido como motor de processos;
10. existir o conceito de Automation Requirement;
11. existir o conceito de Automation Capability;
12. existir o conceito de Capability Registry;
13. existir arquitetura para geração assistida de serviços;
14. existir arquitetura para configuração de eventos e correlações;
15. existir arquitetura para deployment;
16. o processo de Vinculação de Orientação puder ser inteiramente explicado pela arquitetura;
17. a solução não estiver acoplada especificamente à Vinculação de Orientação;
18. GMS e CIR continuarem funcionais;
19. o projeto continuar compilando;
20. decisões importantes estiverem registradas em ADRs;
21. tiver sido criada, se tecnicamente viável, uma prova de conceito do editor BPMN incorporado ao ADE.

---

# 32. Procedimento para o Codex

## Fase A — Discovery

Primeiro apenas analisar.

Não executar grandes refatorações.

Produzir:

```text
current-state.md
```

---

## Fase B — Architecture

Produzir documentação da arquitetura alvo.

---

## Fase C — Embedded Modeler Spike

Investigar e, se possível, criar uma prova de conceito usando:

```text
camunda-bpmn-js
```

compatível com Camunda 7.

---

## Fase D — Gap Analysis

Identificar diferenças entre sistema atual e arquitetura alvo.

---

## Fase E — Minimal Refactoring

Realizar apenas alterações necessárias para criar a base arquitetural.

---

## Fase F — Skeletons

Criar apenas skeletons essenciais:

```text
ADE
Capability Registry
PPG Management Service
```

se adequados à estrutura existente.

---

## Fase G — Validation

Executar:

- build;
- testes;
- inicialização dos componentes quando possível;
- validação da prova de conceito do modelador.

---

## Fase H — Report

Criar:

```text
docs/sprints/E01-report.md
```

contendo:

- estado inicial;
- arquitetura criada;
- decisões;
- arquivos criados;
- arquivos modificados;
- alterações no GMS;
- alterações no CIR;
- prova de conceito BPMN;
- gaps;
- dívida técnica;
- riscos;
- recomendações para E02.

---

# 33. Diretriz final

O propos26 não deve ser pensado apenas como:

```text
Camunda + vários serviços
```

Ele deve ser pensado como:

```text
AMBIENTE ASSISTIDO DE DESENVOLVIMENTO DE AUTOMAÇÕES
                    +
            MOTOR DE PROCESSOS
                    +
         SERVIÇOS REUTILIZÁVEIS
                    +
       INFRAESTRUTURA DE INTEGRAÇÃO
                    +
          SISTEMAS DE DOMÍNIO
```

A arquitetura deve caminhar para permitir que uma pessoa com conhecimento limitado de programação consiga transformar um BPMN em uma automação executável.

O princípio orientador deve ser:

> **Modelar primeiro; configurar quando possível; gerar código quando necessário; programar manualmente apenas como último recurso.**



# Automation Deployment Pipeline

O propos26 deverá adotar como princípio arquitetural que uma **automação é uma unidade composta de implantação**, formada pelo processo BPMN, pelos serviços necessários à sua execução e pelas configurações de integração associadas.

Portanto, colocar uma automação em operação **não significa apenas realizar o deployment do arquivo BPMN no Camunda 7**.

Uma automação poderá ser composta por:

```text
Automation Deployment
│
├── BPMN Process
│
├── Automation Services
│   ├── REST Services
│   └── External Task Workers
│
├── CIR Configuration
├── GMS Configuration
├── Capability Bindings
├── Correlation Definitions
├── Environment Configuration
└── Deployment Manifest
```

## Serviços como unidades independentes de execução

Os componentes responsáveis pela execução das funcionalidades requeridas pelo BPMN deverão, preferencialmente, ser implementados como **serviços independentes**.

A comunicação entre os componentes deverá ocorrer através de contratos explícitos, utilizando principalmente:

```text
REST APIs
External Tasks
Events / Messages
```

O Camunda 7 deverá permanecer responsável pela **orquestração**, enquanto os serviços serão responsáveis pela execução das capacidades utilizadas pelo processo.

Exemplo:

```text
BPMN Service Task
       ↓
Automation Capability
       ↓
REST Service / External Task Worker
       ↓
resultado
       ↓
Camunda
```

Essa separação deverá permitir que uma mesma capacidade seja reutilizada por diferentes processos.

---

## Containerização

Os componentes executáveis do ecossistema deverão ser preparados para execução em **containers Docker**.

Isso inclui progressivamente:

```text
Camunda 7
GMS
CIR
ADE
PPG Management Service
Automation Services
External Task Workers
```

Um serviço gerado pelo ADE deverá, sempre que aplicável, possuir os artefatos necessários para sua containerização.

Por exemplo:

```text
generated-service/
├── pom.xml
├── src/
├── Dockerfile
├── README.md
└── service-definition.yaml
```

O Docker não deverá ser tratado apenas como mecanismo para facilitar o desenvolvimento local.

A **containerização faz parte do modelo de implantação das automações do propos26**.

---

## Pipeline de implantação

O ADE deverá futuramente disponibilizar uma operação conceitual:

```text
Deploy Automation
```

Essa operação deverá coordenar um pipeline semelhante a:

```text
BPMN + Automation Configuration
              ↓
        Validate Automation
              ↓
       Resolve Capabilities
              ↓
      Build Required Services
              ↓
           Run Tests
              ↓
      Build Docker Images
              ↓
      Deploy/Start Containers
              ↓
      Configure GMS and CIR
              ↓
        Deploy BPMN
              ↓
          Camunda 7
              ↓
       Validate Deployment
              ↓
        Automation READY
```

O pipeline deverá distinguir serviços já disponíveis no ambiente daqueles criados especificamente para a nova automação.

Uma capability já implantada e reutilizável **não deverá gerar desnecessariamente uma nova imagem ou um novo container**.

---

## Deployment Manifest

Introduzir conceitualmente um:

```text
Automation Deployment Manifest
```

O manifesto deverá descrever todos os elementos necessários para colocar uma automação em operação.

Exemplo conceitual:

```yaml
automation:
  id: vinculacao-orientacao
  version: 1.0

process:
  file: vinculacao-orientacao.bpmn
  engine: camunda7

services:
  - capability: VALIDATE_REQUEST
    implementation: validate-request-service
    deployment: docker

  - capability: REGISTER_ADVISORSHIP
    implementation: ppg-management-service
    deployment: existing

integrations:
  - type: email
    provider: GMS

inboundEvents:
  - event: CONFIRMACAO_ESTUDANTE
    router: CIR
    correlationKey: requestId

environment:
  type: docker
```

A estrutura definitiva do manifesto deverá ser definida em sprint posterior. Neste momento, o objetivo é estabelecer o conceito arquitetural.

---

## Ambientes de execução

A arquitetura deverá permitir futuramente diferentes ambientes:

```text
DEVELOPMENT
TEST
PRODUCTION
```

Uma mesma automação deverá poder ser promovida entre ambientes sem alterar sua lógica BPMN.

Informações específicas do ambiente, como:

```text
URLs
ports
credentials
database connections
email accounts
Camunda endpoints
```

não deverão ser codificadas diretamente no BPMN ou no código dos serviços.

Essas informações deverão ser fornecidas por configuração externa.

---

## Princípio de deployment

O ADE deverá tratar:

```text
Automation Project
```

como uma unidade lógica e:

```text
Automation Deployment
```

como uma unidade operacional.

Assim:

```text
Automation Project
        ↓
       build
        ↓
Automation Deployment
        ↓
 ┌──────┼─────────┬──────────┐
 ▼      ▼         ▼          ▼
BPMN  Services   CIR/GMS   Configuration
 │      │
 ▼      ▼
Camunda Docker Containers
```

O objetivo final é permitir que o usuário não precise conhecer individualmente os procedimentos técnicos de:

* build Maven;
* criação de Dockerfile;
* criação de imagens;
* inicialização de containers;
* configuração de endpoints;
* deployment via REST do Camunda;
* configuração do CIR;
* configuração do GMS.

Essas operações deverão progressivamente ser **automatizadas pelo ADE**.

## Diretriz arquitetural

O Codex deve considerar, desde o E01, que:

> **uma automação no propos26 é um conjunto versionado e implantável de processo BPMN, serviços, configurações e integrações, executado sobre uma infraestrutura predominantemente containerizada e baseada em chamadas explícitas entre serviços.**

Consequentemente, decisões arquiteturais tomadas neste Sprint não devem impedir a implementação futura de um pipeline automatizado de **build → containerização → configuração → deployment → validação**.



Essa é a direção arquitetural central do propos26.
