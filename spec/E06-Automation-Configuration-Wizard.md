# E06 — Automation Configuration Wizard

## 1. Contexto

O propos26 possui um **Automation Development Environment (ADE)** integrado ao **PPG Management**, no qual o usuário pode criar projetos, modelar processos BPMN, selecionar capabilities, configurar integrações e realizar deployment no Camunda 7.

As sprints anteriores introduziram gestão de projetos, integração com PPG Management, Capability Registry, bindings, mappings, workers/serviços, integrações, deployment e execução.

A configuração, entretanto, ainda exige que o usuário conheça as diferentes áreas do ADE e saiba quais elementos precisam ser configurados.

Esta sprint introduzirá um **Automation Configuration Wizard**, inspirado nos wizards clássicos de IDEs e instaladores. O Wizard será uma funcionalidade **orientada ao processo BPMN**.

Fluxo esperado:

```text
1. Create Automation Project
        ↓
2. Model / Import BPMN Process
        ↓
3. Run Automation Configuration Wizard
        ↓
4. Configure process element by element
        ↓
5. Validate complete automation
        ↓
6. Finish Wizard
        ↓
7. Project READY FOR DEPLOYMENT
        ↓
8. Deploy
```

Nesta sprint **não utilizar IA generativa**. Sugestões deverão ser produzidas por regras determinísticas, metadados, tipos BPMN, capabilities existentes e contratos.

## 2. Objetivo principal

Implementar no ADE um Wizard visual e interativo que:

1. analise o BPMN do projeto ativo;
2. identifique elementos que necessitam configuração ou validação;
3. determine os Automation Requirements correspondentes;
4. construa uma sequência de etapas;
5. apresente cada elemento ao usuário;
6. verifique sua configuração atual;
7. proponha configurações iniciais deterministicamente quando possível;
8. permita selecionar ou alterar configurações;
9. valide cada etapa;
10. persista as configurações no modelo do projeto;
11. execute validação global ao final;
12. deixe o projeto completamente configurado e validado para deployment.

O Wizard **não deverá criar um segundo modelo de configuração**. Ele deverá editar as mesmas estruturas utilizadas pelas áreas `Automation`, `Capabilities`, `Integrations` e `Deployment`.

## 3. Princípio arquitetural fundamental

```text
                   AutomationProject
                         ▲
                         │
              reads / updates
                         │
                 Configuration Wizard
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
   Automation       Capabilities     Integrations
```

Não duplicar `CapabilityBinding`, `VariableMapping`, `IntegrationDefinition`, `CorrelationDefinition`, `MessageDefinition`, `DeploymentConfiguration` ou `AutomationRequirement`.

Configuração criada no Wizard deverá aparecer imediatamente nas telas normais. Alteração feita fora do Wizard deverá ser reconhecida ao executá-lo novamente.

## 4. Entrada no Wizard

Adicionar ao projeto ativo:

```text
Run Configuration Wizard
```

Quando houver progresso anterior:

```text
Resume Configuration Wizard
```

Também permitir revisão:

```text
Review Configuration
```

O Wizard sempre trabalha sobre o projeto atualmente aberto.

## 5. Aparência geral — requisito obrigatório

A experiência visual é requisito central desta sprint.

O Wizard deverá ser uma **janela flutuante/modal própria**, visualmente destacada do restante do ADE.

Não implementar como página comum, formulário administrativo simples, accordion, painel lateral improvisado, `alert`, `confirm` ou dialog HTML sem acabamento visual.

Referência conceitual:

```text
┌───────────────────────────────────────────────────────────────┐
│ Automation Configuration Wizard                         ✕     │
│ Cancelamento de Orientação                                    │
├───────────────────────────────────────────────────────────────┤
│                                                               │
│  ✓ Start Message Event      │                                 │
│  ✓ Find Student             │                                 │
│  → Find Advisorship         │   Current element               │
│  ○ Gateway                  │                                 │
│  ○ Send Confirmation        │   Configuration controls        │
│  ○ Wait Response            │                                 │
│  ○ Cancel Advisorship       │                                 │
│                             │                                 │
├───────────────────────────────────────────────────────────────┤
│ Step 3 of 7                     [ Back ] [ Cancel ] [ Next ]  │
└───────────────────────────────────────────────────────────────┘
```

A representação é conceitual. Adaptar o design ao sistema visual existente no ADE.

## 6. Visual and Interaction Design Requirements

### 6.1 Modal flutuante

A janela deverá:

- ficar centralizada;
- aparecer sobre o ADE;
- possuir backdrop que reduza a atenção sobre a interface de fundo;
- possuir sombra e acabamento visual consistente;
- ter dimensões confortáveis para desktop;
- evitar aparência excessivamente compacta;
- manter boa hierarquia tipográfica;
- permitir conteúdo interno rolável quando necessário.

Não criar aplicação ou janela nativa do sistema operacional.

### 6.2 Estrutura visual

A janela deverá possuir três regiões estáveis:

```text
Header
Main Content
Footer
```

O Header deverá mostrar nome do Wizard, projeto atual e opção de fechamento/cancelamento quando apropriado.

O Main Content deverá conter progresso/navegação, identificação do elemento, diagnóstico e configuração.

O Footer deverá permanecer visível e conter:

```text
Back
Cancel
Next
```

Na etapa final:

```text
Back
Cancel
Finish
```

### 6.3 Estados visuais

Representar claramente:

```text
CURRENT
CONFIGURED
WARNING
ERROR
PENDING
```

Não depender apenas de cor para comunicar estado.

### 6.4 Transições

Mudanças entre etapas deverão ser suaves e discretas. Evitar animações excessivas.

### 6.5 Responsividade

Priorizar desktop, garantindo comportamento razoável em resoluções menores sem comprometer footer, botões, campos, mappings e progresso.

### 6.6 Qualidade visual como critério de aceite

O Wizard não será considerado concluído apenas por funcionar tecnicamente. Deverá apresentar acabamento compatível com ferramenta moderna de desenvolvimento: espaçamento, tipografia, alinhamento, estados, feedback, navegação, componentes e hierarquia visual.

## 7. Análise inicial do BPMN

Ao executar o Wizard, analisar o BPMN atual e identificar elementos relevantes, por exemplo:

```text
Start Message Event
Service Task
Send Task
Receive Task
Intermediate Message Catch Event
Intermediate Message Throw Event
Exclusive Gateway
End Event
```

Outros tipos já suportados pelo ADE poderão ser incluídos.

Nem todo elemento exige configuração complexa; alguns poderão exigir apenas validação.

## 8. Automation Requirements

O Wizard deverá trabalhar preferencialmente sobre `AutomationRequirement`.

Exemplos:

```text
Service Task
    ↓
CAPABILITY_BINDING
VARIABLE_MAPPING
```

```text
Send Task
    ↓
OUTBOUND_COMMUNICATION
CAPABILITY_BINDING
```

```text
Message Catch Event
    ↓
INBOUND_EVENT
MESSAGE_DEFINITION
CORRELATION_DEFINITION
```

```text
Gateway
    ↓
CONDITION_VALIDATION
```

Consolidar uma camada:

```text
BPMN Element
      ↓
Automation Requirement Analysis
      ↓
Wizard Step Definition
```

Evitar regras espalhadas diretamente nos componentes visuais.

## 9. Construção das etapas

Depois da análise, criar sequência ordenada, refletindo sempre que possível a ordem lógica do processo.

Exemplo:

```text
1. Start Message Event
2. Find Student
3. Find Advisorship
4. Advisorship Exists?
5. Send Confirmation
6. Wait Confirmation
7. Cancel Advisorship
8. End Event
```

Elementos sem necessidade de interação poderão aparecer como validados, ser tratados automaticamente ou ser omitidos da sequência interativa, desde que participem da validação global.

Documentar a estratégia utilizada.

## 10. Navegação lateral/progresso

Adicionar área visual de progresso:

```text
✓ Receive Request
✓ Find Student
→ Find Advisorship
○ Advisorship Exists?
○ Send Confirmation
○ Wait Confirmation
○ Cancel Advisorship
```

Ela deverá indicar onde o usuário está, o que já foi configurado, o que falta e onde existem problemas.

Navegação arbitrária clicando em qualquer etapa é opcional nesta versão.

## 11. Configuração de Service Task

Apresentar pelo menos:

```text
Element
BPMN ID
Name
Configuration status
Automation Requirements
Capability
Implementation
Input mappings
Output mappings
Validation
```

Exemplo:

```text
Service Task
Find Advisorship

BPMN
✓ ID defined
✓ Name defined

Automation
! Capability not configured

Suggested Capability
FIND_ADVISORSHIP_BY_STUDENT

Capability
[ FIND_ADVISORSHIP_BY_STUDENT ▼ ]

Input Mapping
studentId ← [ ${studentId} ▼ ]

Output Mapping
id     → [ advisorshipId ]
status → [ advisorshipStatus ]

Implementation
External Task Worker
```

## 12. Sugestão determinística de capability

Não utilizar LLM ou IA generativa.

Sugestões poderão utilizar:

- tipo BPMN;
- Automation Requirement;
- capability já vinculada;
- capabilities compatíveis;
- tipos de entrada/saída;
- metadados;
- nomes técnicos;
- configuração existente.

Uma sugestão nunca deverá ser aplicada silenciosamente quando alterar semanticamente a automação.

Mostrar explicitamente `Suggested Capability` e permitir confirmação/substituição.

## 13. Seleção de capability

A seleção deverá ser visualmente rica. Evitar um `select` gigantesco sem contexto.

Mostrar sempre que possível:

```text
Capability name
Provider
Description
Interface type
Availability/status
```

Exemplo:

```text
CANCEL_ADVISORSHIP
PPG Management · REST
Cancels an active advisorship preserving its history.
```

Permitir busca/filtro quando necessário.

## 14. Variable Mapping

Criar componente visual adequado.

```text
Capability Input          Process Variable
studentId          ←      ${student.id}
advisorshipId      ←      ${advisorship.id}
```

Outputs:

```text
Capability Output         Process Variable
status             →      advisorshipStatus
updatedAt          →      advisorshipUpdatedAt
```

Validar obrigatoriedade, tipos quando conhecidos, variável inexistente quando detectável e mappings inconsistentes.

## 15. Send Task / Outbound Communication

Permitir configuração guiada:

```text
Send Task
Request Student Confirmation

Capability
SEND_EMAIL

To
[ ${student.email} ]

Subject
[ Confirmação de cancelamento de orientação ]

Body / Template
[ ... ]

Expected response
[ CONFIRMACAO_ESTUDANTE ]
```

Utilizar as estruturas de outbound communication existentes no ADE.

## 16. Message Catch Event / Receive Task

Apresentar:

```text
Message
Inbound Channel
Provider
Router
External Event
Correlation Key
Target Process Variable
```

Exemplo:

```text
Message Catch Event
Wait Student Confirmation

Inbound Channel
EMAIL

Provider
GMS

Router
CIR

External Event
CONFIRMACAO_ESTUDANTE

Correlation
requestId ← ${requestId}
```

Validar consistência entre BPMN Message, CIR, GMS, correlation key e variáveis.

## 17. Start Message Event

Quando iniciado por mensagem, configurar:

```text
Inbound channel
Event type
Message name
Process key
Initial variable mappings
Business key / correlation identifier
```

Utilizar abstrações existentes do CIR/GMS.

## 18. Gateways

Gateways deverão participar da validação.

Para Exclusive Gateway, verificar pelo menos condições necessárias, fluxo default quando aplicável e expressões válidas no contexto suportado.

Não criar capability para gateway quando não houver necessidade.

## 19. Elementos já configurados

Ao reexecutar o Wizard, carregar valores atuais e indicar:

```text
✓ Capability configured
✓ Mappings configured
✓ Integration configured
```

Não sobrescrever configurações existentes apenas porque foi encontrada sugestão diferente.

## 20. Validação por etapa

Classificar problemas:

```text
ERROR
WARNING
INFO
```

`ERROR` poderá impedir `Next` quando indispensável. `WARNING` poderá permitir avanço.

Exemplo:

```text
ERROR
Required input "studentId" is not mapped.

WARNING
No output variable was defined for "updatedAt".
```

## 21. Back

`Back` retorna à etapa anterior preservando configurações e sem desfazer automaticamente alterações.

## 22. Cancel

`Cancel` interrompe o Wizard sem destruir configurações válidas já persistidas. Se houver alterações ainda não persistidas, solicitar confirmação apropriada.

## 23. Resume Wizard

Persistir informação suficiente:

```text
wizardState
lastConfiguredElement
lastRunAt
```

Permitir:

```text
[ Start Over ] [ Resume ]
```

`Start Over` reinicia a navegação/revisão, não apaga silenciosamente configurações existentes.

## 24. Etapa final — Global Validation

Antes de `Finish`, executar validação completa.

Exemplo:

```text
Automation Validation

BPMN
✓ Valid

Service Tasks
✓ 3 / 3 configured

Inbound Events
✓ 2 / 2 configured

Outbound Communications
✓ 2 / 2 configured

Capabilities
✓ All resolved

Variable Mappings
✓ Valid

Correlation Rules
✓ Valid

External Services
✓ PPG Management
✓ GMS
✓ CIR
✓ Camunda

Deployment Readiness
READY
```

Com problemas:

```text
NOT READY
2 errors
1 warning
```

Permitir identificar e retornar ao elemento responsável.

## 25. Verificação de serviços

Quando tecnicamente viável, verificar disponibilidade de:

```text
Camunda
PPG Management
GMS
CIR
```

Usar health endpoints existentes e diferenciar validade da configuração de disponibilidade runtime.

## 26. Finish

`Finish` somente habilitado sem erros impeditivos.

Ao concluir:

1. persistir configurações;
2. atualizar estado do Wizard;
3. executar validação final;
4. atualizar estado do projeto para equivalente a `READY_FOR_DEPLOYMENT`.

Não realizar deployment automaticamente.

```text
Finish Wizard
      ↓
READY FOR DEPLOYMENT
      ↓
User chooses Deploy
```

## 27. Sincronização com Automation

Tudo configurado no Wizard deverá ser imediatamente visível em `Automation` e demais áreas correspondentes. O inverso também é obrigatório.

## 28. Alterações posteriores no BPMN

Se o BPMN mudar após conclusão:

```text
READY_FOR_DEPLOYMENT
        ↓
BPMN changed
        ↓
configuration must be revalidated
```

Invalidar ou marcar readiness como desatualizado quando a mudança puder afetar configuração, por exemplo `CONFIGURATION_REVIEW_REQUIRED`.

## 29. Alterações posteriores de configuração

Alterações manuais em capability, mappings, mensagens, correlações ou integrações deverão atualizar o estado de validação quando necessário.

## 30. Caso principal de validação

Usar `Cancelamento de Orientação` sem código específico no Wizard.

Exercitar pelo menos:

- início por mensagem;
- consulta de estudante/orientação;
- Service Task;
- capability do PPG Management;
- gateway;
- envio de e-mail;
- espera de resposta;
- correlação;
- cancelamento da orientação;
- conclusão.

## 31. Segundo caso de validação

Executar sobre `Vinculação de Orientação` para comprovar genericidade.

## 32. Caso de defesa

Quando possível, usar cenário mínimo para validar:

```text
UPLOAD_DISSERTATION
GENERATE_DISSERTATION_DOWNLOAD_LINK
SEND_EMAIL
```

Não é necessário construir todo o processo de defesa nesta sprint.

## 33. Não utilizar IA generativa

É proibido tornar o Wizard dependente de LLM, OpenAI API, geração por prompt ou interpretação semântica generativa.

A arquitetura poderá ser preparada para sugestões inteligentes futuras, mas o comportamento atual deve ser determinístico e testável.

## 34. Genericidade

Não criar:

```text
CancelAdvisorshipWizard
DefenseWizard
AdvisorshipWizard
```

Preferir abstrações como:

```text
AutomationConfigurationWizard
WizardSession
WizardStep
WizardStepStatus
ElementConfiguration
ElementValidator
RequirementResolver
CapabilitySelector
VariableMappingEditor
IntegrationConfigurator
GlobalAutomationValidator
```

Adaptar nomes à arquitetura existente.

## 35. Persistência

A sessão pode possuir estado próprio de navegação, mas a configuração funcional pertence ao `AutomationProject`.

```text
WizardSession
├── projectId
├── currentStep
├── status
├── startedAt
├── lastUpdatedAt
└── stepStatuses
```

Não duplicar dados funcionais do projeto na sessão.

## 36. BPMN alterado durante o Wizard

Evitar inconsistência. A solução poderá impedir edição simultânea ou detectar alteração e solicitar reinicialização da análise. Preferir a solução mais simples e segura.

## 37. Acessibilidade e interação

Garantir foco inicial apropriado, navegação razoável por teclado, `Esc` sem perda silenciosa, labels associados, feedback textual além de cores e motivo compreensível para botões desabilitados.

## 38. Componentização visual

Criar componentes reutilizáveis, por exemplo:

```text
WizardShell
WizardHeader
WizardProgress
WizardFooter
ElementSummary
ValidationSummary
CapabilityPicker
VariableMappingEditor
MessageConfigurator
CorrelationConfigurator
IntegrationStatus
```

Evitar componente monolítico.

## 39. Testes

### Navegação

- abrir;
- Next;
- Back;
- Cancel;
- Resume;
- Finish.

### Persistência

- configuração do Wizard aparece em Automation;
- configuração de Automation aparece no Wizard;
- fechar/reabrir preserva configuração;
- retomar preserva progresso.

### Validação

- input obrigatório ausente;
- capability ausente;
- correlation ausente;
- BPMN inválido;
- configuração válida;
- serviços indisponíveis.

### Reexecução

- projeto já configurado;
- BPMN alterado;
- revalidação.

## 40. Critérios de aceitação funcionais

1. existir `Run Configuration Wizard`;
2. analisar BPMN;
3. identificar elementos relevantes;
4. criar sequência de configuração;
5. permitir Back/Next;
6. configurar capabilities em Service Tasks;
7. configurar mappings;
8. configurar Send Tasks/comunicações;
9. configurar Message Catch Events;
10. configurar correlações;
11. reconhecer configurações existentes;
12. persistir no projeto;
13. telas normais refletirem alterações do Wizard;
14. Wizard refletir alterações das telas normais;
15. executar validação global;
16. Finish somente sem erros impeditivos;
17. projeto atingir estado equivalente a `READY_FOR_DEPLOYMENT`;
18. Wizard poder ser retomado/reexecutado;
19. Cancelamento de Orientação funcionar sem código específico;
20. Vinculação de Orientação também ser analisável/configurável.

## 41. Critérios de aceitação visual

Todos obrigatórios:

1. janela flutuante/modal;
2. backdrop sobre o ADE;
3. header visualmente claro;
4. footer fixo;
5. Back/Cancel/Next/Finish claramente posicionados;
6. indicador de progresso;
7. estado atual claramente identificável;
8. configured/warning/error/pending distinguíveis;
9. mappings organizados visualmente;
10. capability selection com informação contextual;
11. bom uso de espaço;
12. ausência de aparência de formulário administrativo improvisado;
13. consistência com identidade visual do ADE;
14. acabamento adequado a ferramenta moderna de desenvolvimento.

**Uma implementação funcional porém visualmente pobre não atende aos critérios de aceite desta sprint.**

## 42. Restrições

- não migrar para Camunda 8;
- não substituir GMS ou CIR;
- não reconstruir Capability Registry sem necessidade;
- não criar lógica específica de processo;
- não usar IA generativa;
- não realizar refatorações amplas sem relação com o Wizard;
- não duplicar o modelo do AutomationProject;
- não transformar `Finish` automaticamente em deployment.

## 43. Estratégia de implementação para o Codex

### Fase 1 — Inspeção

Antes de alterar código:

1. revisar resultados de E03, E04 e E05;
2. localizar frontend do ADE;
3. localizar AutomationProject;
4. localizar Capability Registry;
5. localizar CapabilityBinding;
6. localizar VariableMapping;
7. localizar integrações;
8. localizar validação/deployment;
9. identificar framework/component library visual existente.

Não substituir a stack visual sem necessidade.

### Fase 2 — Modelo do Wizard

Implementar sessão, steps, status, análise BPMN, RequirementResolver e validação.

### Fase 3 — Shell visual

Construir primeiro a janela completa com modal, header, progress, content, footer e estados. Validar aparência antes de preencher todos os tipos.

### Fase 4 — Configuradores

Adicionar progressivamente:

1. Service Task;
2. capability selection;
3. variable mapping;
4. outbound communication;
5. inbound message;
6. correlation;
7. gateway validation.

### Fase 5 — Sincronização

Garantir Wizard e telas normais sobre o mesmo modelo.

### Fase 6 — Validação global

Implementar summary e readiness.

### Fase 7 — Testes

Executar:

```text
Cancelamento de Orientação
Vinculação de Orientação
```

e, quando viável, cenário mínimo com dissertação.

## 44. Documentação

Criar:

```text
docs/e06/overview.md
docs/e06/wizard-architecture.md
docs/e06/wizard-ui.md
docs/e06/requirement-resolution.md
docs/e06/validation.md
docs/e06/test-scenarios.md
```

A documentação visual deverá incluir screenshots da implementação final.

## 45. Relatório do sprint

Criar:

```text
docs/sprints/E06-report.md
```

Informar arquitetura, componentes, regras de análise BPMN, elementos suportados/não suportados, validações, persistência, sincronização, screenshots, testes, limitações, dívida técnica e recomendações para E07.

## 46. Resultado esperado

```text
Open ADE
   ↓
New Project
   ↓
Cancelamento de Orientação
   ↓
Model / Import BPMN
   ↓
Run Configuration Wizard
   ↓
Configure each relevant BPMN element
   ↓
Select capabilities
   ↓
Map variables
   ↓
Configure messages
   ↓
Configure correlations
   ↓
Review validation
   ↓
Finish
   ↓
READY FOR DEPLOYMENT
```

O usuário deverá conseguir realizar esse fluxo sem descobrir manualmente em quais telas do ADE cada configuração deve ser realizada.

O Wizard deverá transformar a configuração técnica da automação em um **processo guiado, progressivo, verificável e visualmente bem acabado**, mantendo toda a configuração integrada ao modelo normal do projeto.
