# E09 — Descoberta Dinâmica de Contratos e Extração de Dados de Entrada

## 1. Objetivo

Evoluir o ADE para que a configuração das automações não dependa de conhecimento hardcoded sobre operações do sistema de domínio.

O ADE deverá, em tempo de execução:

1. descobrir operações disponibilizadas pelos sistemas integrados;
2. obter os contratos de entrada e saída;
3. normalizar esses contratos como capabilities;
4. determinar os dados necessários para executar cada capability;
5. comparar esses requisitos com os dados já existentes no processo;
6. orientar a obtenção dos dados ausentes a partir de mensagens, lookups ou transformações;
7. configurar GMS/CIR quando a origem for uma mensagem externa;
8. disponibilizar os dados no `ProcessDataContext`;
9. reagir a novas operações ou alterações de contrato sem rebuild do ADE.

O processo **Cadastro de Defesa** será o caso de referência, mas nenhuma regra da solução poderá ser específica desse processo ou do PPG-ADM.

---

## 2. Problema atual

Hoje uma nova operação do PPG-ADM pode exigir alteração do ADE para que seus parâmetros apareçam no Wizard:

```text
PPG-ADM adiciona endpoint
        ↓
ADE não conhece os parâmetros
        ↓
Wizard não conhece os dados necessários
        ↓
programador altera ADE
        ↓
rebuild/deploy
```

O comportamento desejado é:

```text
PPG-ADM publica/altera operação
        ↓
ADE consulta contrato em runtime
        ↓
Capability Registry é atualizado
        ↓
Wizard descobre os requisitos de dados
        ↓
usuário configura suas origens
        ↓
GMS/CIR extrai os dados quando aplicável
        ↓
variáveis ficam disponíveis ao processo
```

---

## 3. Separação de responsabilidades

O sistema de domínio declara **o que a operação precisa receber e o que produz**.

O ADE determina **de onde virão os dados necessários dentro da automação**.

O GMS recebe e normaliza mensagens externas.

O CIR interpreta/roteia os eventos e, conforme a arquitetura existente, aplica a configuração de extração necessária para produzir o payload estruturado do processo.

O Camunda orquestra o processo usando as variáveis produzidas.

```text
DOMAIN SYSTEM
   contrato da operação
          ↓
CAPABILITY DISCOVERY
          ↓
ADE
   resolução das origens dos dados
          ↓
EXTRACTION / LOOKUP / TRANSFORMATION
          ↓
PROCESS VARIABLES
          ↓
CAMUNDA
          ↓
CAPABILITY INVOCATION
```

---

## 4. Caso de referência — Cadastro de Defesa

Considere que o contrato atual da operação de cadastro de defesa exija:

```text
studentId        : Long
advisorId        : Long
title            : String
date             : Date
location         : String
committeeMembers : Array<CommitteeMember>
```

Esses campos são **caso de teste**, não configuração fixa.

É proibido implementar lógica equivalente a:

```text
if capability == CREATE_DEFENSE:
    fields = [...]
```

Os requisitos devem surgir do contrato descoberto em runtime.

---

## 5. Descoberta do contrato do sistema de domínio

Antes da implementação, inspecionar o PPG-ADM e identificar o mecanismo real de descrição da API.

Preferir OpenAPI 3.x quando já publicado, por exemplo `/v3/api-docs`, mas **não assumir uma URL sem verificar o sistema existente**.

Criar uma abstração como:

```text
DomainContractDiscovery
```

Responsabilidades:

```text
discover provider
discover operations
discover schemas
resolve references
normalize operation contract
```

A descoberta deverá ocorrer pelo backend, não diretamente pelo frontend do Wizard.

---

## 6. Runtime Capability Discovery

Fluxo:

```text
ADE Backend
    ↓
Provider metadata / OpenAPI
    ↓
parse operations
    ↓
resolve request/response schemas
    ↓
normalize contracts
    ↓
Capability Registry
```

Uma nova operação publicada pelo provider deverá aparecer no Registry sem recompilar o ADE.

---

## 7. Capability Registry dinâmico

A capability normalizada deverá conter, quando aplicável:

```text
id
name
description
provider
operationId
protocol
method
endpoint
inputSchema
outputSchema
contractVersion/fingerprint
lastDiscoveredAt
```

O formato interno pode seguir os padrões já existentes no projeto.

O Wizard não deverá depender diretamente de OpenAPI; ele consome o contrato normalizado do Capability Registry.

---

## 8. Tipos e schemas complexos

Suportar pelo menos:

```text
String
Boolean
Integer
Long
Decimal/Number
Date
DateTime
Enum
Object
Array<T>
```

O caso abaixo é obrigatório:

```text
committeeMembers : Array<CommitteeMember>
```

Resolver `$ref` e schemas aninhados.

Os atributos reais de `CommitteeMember` devem ser descobertos no contrato do PPG-ADM, nunca definidos no ADE apenas para satisfazer o exemplo.

---

## 9. Contrato da capability não significa variável existente

Descobrir:

```text
CREATE_DEFENSE requires:
studentId
advisorId
title
date
location
committeeMembers
```

significa apenas que esses valores **precisam existir antes da execução da Service Task**.

Não significa que já sejam variáveis do processo.

O Wizard deve comparar:

```text
Required Capability Inputs
            VS
Available Process Data
```

---

## 10. DataRequirementResolver

Criar componente conceitual:

```text
DataRequirementResolver
```

Entrada:

```text
Capability Contract
+
ProcessDataContext no ponto da Service Task
```

Resultado por input:

```text
SATISFIED
MISSING
TYPE_MISMATCH
TRANSFORMATION_REQUIRED
LOOKUP_REQUIRED
```

---

## 11. Análise retroativa de requisitos

O Wizard deverá relacionar capabilities posteriores com fontes anteriores.

Exemplo:

```text
Receber solicitação
       ↓
...
       ↓
Cadastrar defesa
       ↓
CREATE_DEFENSE
```

Se a capability exigir dados ainda inexistentes, o ADE deverá informar na etapa adequada:

> A funcionalidade **Cadastrar defesa** necessita de informações que ainda não são produzidas pelo processo. Como a solicitação chega por e-mail, algumas delas podem ser obtidas da mensagem recebida.

A análise deve ser genérica e não assumir que todo dado ausente deve vir do Start Event.

---

## 12. ProcessInputRequirementAnalyzer

Criar componente conceitual:

```text
ProcessInputRequirementAnalyzer
```

Responsabilidades:

1. percorrer o grafo BPMN;
2. localizar Service Tasks e capabilities;
3. carregar seus contratos atuais;
4. verificar o `ProcessDataContext` antes de cada tarefa;
5. identificar inputs não satisfeitos;
6. rastrear possíveis produtores anteriores;
7. identificar requisitos que precisam de nova origem;
8. apresentar esses gaps no Wizard.

---

## 13. Quatro operações distintas sobre dados

O Wizard deve distinguir claramente:

### Mapping

Um valor já existe e é associado a um parâmetro compatível.

```text
process.studentId
       ↓
CREATE_DEFENSE.studentId
```

### Extraction

Um novo valor é obtido de uma fonte.

```text
email.body
       ↓
extract
       ↓
title
```

### Transformation

Um valor existente precisa ser convertido.

```text
"05/11/2026"
       ↓
parse Date
       ↓
date:Date
```

### Lookup

Um dado é utilizado para localizar outro.

```text
advisorEmail
       ↓
FIND_PROFESSOR_BY_EMAIL
       ↓
advisorId
```

Não tratar essas quatro situações como simples variable mapping.

---

## 14. IDs não devem ser inventados a partir do e-mail

O endpoint pode exigir:

```text
studentId : Long
advisorId : Long
```

mas o e-mail pode conter apenas:

```text
studentEmail
advisorEmail
```

Nesse caso, a resolução correta pode ser:

```text
studentEmail
    ↓
FIND_STUDENT_BY_EMAIL
    ↓
studentId
```

```text
advisorEmail
    ↓
FIND_PROFESSOR_BY_EMAIL
    ↓
advisorId
```

O Wizard deve diferenciar dado extraível, transformável, obtido por lookup e inexistente.

---

## 15. Wizard orientado pelos requisitos descobertos

Ao selecionar `CREATE_DEFENSE`, apresentar:

### Dados necessários para cadastrar a defesa

> O sistema PPG-ADM informa que esta operação necessita dos seguintes dados:

```text
studentId        Long
advisorId        Long
title            String
date             Date
location         String
committeeMembers CommitteeMember[]
```

> Esses requisitos foram obtidos automaticamente do contrato atual do PPG-ADM.

Não mostrar esses campos porque foram programados na UI.

---

## 16. Resolver a origem de cada requisito

Para cada input ainda não satisfeito:

```text
studentId : Long

Como este valor será obtido?

[ Usar dado existente ]
[ Extrair da mensagem ]
[ Transformar outro dado ]
[ Obter usando outra funcionalidade ]
```

As opções devem ser filtradas pelo tipo e pelo contexto.

Se nenhuma estratégia válida estiver disponível, explicar o gap em vez de permitir mapping inválido.

---

## 17. Configuração da extração de e-mail

Quando a origem for EMAIL, não expor JSON, regex ou `${...}` como interface principal.

Exemplo:

### Informações que precisam ser obtidas da mensagem

```text
Título da defesa
Tipo: String
[ Obter do corpo do e-mail ▼ ]

Data da defesa
Tipo: Date
[ Obter do corpo do e-mail ▼ ]

Local
Tipo: String
[ Obter do corpo do e-mail ▼ ]

Membros da banca
Tipo: CommitteeMember[]
[ Obter do corpo do e-mail ▼ ]
```

---

## 18. InputExtractionContract

O Wizard deverá gerar uma configuração estruturada de extração, conceitualmente:

```text
InputExtractionContract
```

Exemplo lógico:

```yaml
source:
  channel: EMAIL
  part: BODY
outputs:
  - name: title
    type: String
    required: true
  - name: date
    type: Date
    required: true
  - name: location
    type: String
    required: true
  - name: committeeMembers
    type: Array<CommitteeMember>
    required: true
```

Usar o formato concreto mais adequado à arquitetura existente.

---

## 19. GMS e CIR

Antes de alterar responsabilidades, inspecionar o código atual.

A arquitetura preferida é:

```text
GMS
raw/normalized message
       ↓
CIR
configured extraction/routing
       ↓
structured event payload
       ↓
Camunda
```

### GMS

Responsável por receber e normalizar a mensagem, disponibilizando envelope, body, attachments e demais dados suportados.

### CIR

Responsável pelo evento de integração, roteamento e aplicação da configuração de extração quando isso estiver alinhado à arquitetura existente.

### ADE

Responsável por configurar a extração.

Não inserir parsing específico de Cadastro de Defesa no GMS.

---

## 20. Contrato ADE → CIR

Criar mecanismo/API para registrar ou atualizar no CIR a configuração de extração de determinado evento sem rebuild.

Conceitualmente:

```text
ADE
   ↓
PUT/POST extraction configuration
   ↓
CIR
```

O contrato deve referenciar:

- evento;
- canal;
- schema esperado;
- regras/estratégia de extração;
- tipos;
- obrigatoriedade;
- versão/fingerprint quando necessário.

---

## 21. DataExtractionProvider extensível

Criar abstração semelhante a:

```text
DataExtractionProvider
```

Preparar a arquitetura para estratégias como:

```text
StructuredTemplateExtractor
JsonExtractor
FormExtractor
RuleBasedTextExtractor
AITextExtractor
```

Não tornar IA generativa requisito desta sprint.

---

## 22. Texto livre exige estratégia explícita

Não assumir que o CIR consegue obter `studentId`, `title`, `date` etc. apenas porque o contrato os exige.

Distinguir:

```text
Dados do envelope
from / to / subject / attachments
```

versus:

```text
Dados semânticos no corpo
studentEmail / advisorEmail / title / date / location / committeeMembers
```

Inspecionar os mecanismos existentes. Se atualmente não há parser de texto livre, implementar uma estratégia determinística configurável ou documentar explicitamente o gap.

---

## 23. Template estruturado de mensagem

Como estratégia determinística inicial, permitir gerar um template de entrada a partir do schema.

Exemplo conceitual:

```text
Student: <studentEmail>
Advisor: <advisorEmail>
Title: <title>
Date: <date>
Location: <location>

Committee:
- <member>
- <member>
```

O formato real de `CommitteeMember` deve vir do schema descoberto.

Não hardcodar um template de Defesa.

Fluxo:

```text
Capability Input Schema
       ↓
Message Input Template
       ↓
Extraction Contract
```

---

## 24. DataResolutionPlan

Criar representação intermediária persistível:

```text
DataResolutionPlan
```

Exemplo:

```text
studentId
  strategy: CAPABILITY_LOOKUP
  source: studentEmail
  capability: FIND_STUDENT_BY_EMAIL
  output: id

advisorId
  strategy: CAPABILITY_LOOKUP
  source: advisorEmail
  capability: FIND_PROFESSOR_BY_EMAIL
  output: id

title
  strategy: MESSAGE_EXTRACTION

date
  strategy: MESSAGE_EXTRACTION

location
  strategy: MESSAGE_EXTRACTION

committeeMembers
  strategy: MESSAGE_EXTRACTION
```

Persistir no `AutomationProject` ou no modelo canônico já utilizado para configuração.

---

## 25. ProcessDataContext

Depois da extração, o contexto poderá conter, conforme a configuração real:

```text
studentEmail      String
advisorEmail      String
title             String
date              Date
location          String
committeeMembers  CommitteeMember[]
```

Depois dos lookups:

```text
studentId         Long
advisorId         Long
```

Antes de `CREATE_DEFENSE`, todos os inputs obrigatórios deverão estar satisfeitos.

---

## 26. Capability Readiness

A Service Task deverá apresentar algo como:

### Cadastrar defesa

```text
Entradas necessárias
✓ studentId
✓ advisorId
✓ title
✓ date
✓ location
✓ committeeMembers
```

> Todos os dados necessários estarão disponíveis quando esta tarefa for executada.

Se faltar algo:

```text
⚠ advisorId ainda não possui origem definida.
[ Configurar origem ]
```

---

## 27. Evolução do contrato sem rebuild

Manter fingerprint/versão do contrato descoberto.

Se o PPG-ADM adicionar, por exemplo:

```text
defenseType : DefenseType (required)
```

o ADE deve detectar a diferença e marcar a configuração como desatualizada/incompleta usando o modelo de status já existente ou extensão compatível.

Nunca continuar silenciosamente com contrato antigo como se estivesse válido.

---

## 28. Refresh e cache

Implementar ação:

```text
Refresh capabilities
```

Avaliar refresh automático com TTL.

Não consultar OpenAPI a cada renderização do Wizard.

Manter cache e fingerprint.

---

## 29. Provider indisponível

Distinguir:

```text
não foi possível atualizar o contrato
```

de:

```text
capability não existe
```

Quando seguro, utilizar o último contrato conhecido e informar sua data/versão.

---

## 30. Segurança

A descoberta deve ocorrer no backend e apenas contra providers registrados.

Considerar:

- autenticação;
- allowlist;
- SSRF;
- timeout;
- limite de tamanho;
- validação do documento OpenAPI/metadados;
- schemas malformados;
- indisponibilidade do provider.

Não permitir que a UI forneça URLs arbitrárias para consulta.

---

## 31. Não acoplar o Wizard ao OpenAPI

Mesmo usando OpenAPI no PPG-ADM:

```text
OpenAPI / provider metadata
        ↓
DomainContractDiscovery
        ↓
Normalized Capability Contract
        ↓
Capability Registry
        ↓
Wizard
```

Isso permitirá outros mecanismos de descoberta no futuro.

---

## 32. Componentes sugeridos

Avaliar:

```text
DomainContractDiscovery
OpenApiContractDiscovery
CapabilityContractNormalizer
CapabilityRegistrySynchronizer
CapabilitySchema
CapabilityInputSchema
CapabilityOutputSchema
DataRequirementResolver
ProcessInputRequirementAnalyzer
InputExtractionContract
DataResolutionPlan
ExtractionConfigurationService
DataExtractionProvider
MessageTemplateGenerator
ContractFingerprintService
```

Os nomes não são mandatórios.

---

## 33. Fluxo completo esperado

```text
PPG-ADM
  OpenAPI / metadata
       ↓
DomainContractDiscovery
       ↓
Capability Registry
       ↓
CREATE_DEFENSE contract
       ↓
ProcessInputRequirementAnalyzer
       ↓
Required data
       ↓
Wizard
       ↓
DataResolutionPlan
       ↓
Extraction configuration
       ↓
CIR
       ↑
GMS normalized email
       ↓
structured process data
       ↓
ProcessDataContext
       ↓
lookups / transformations
       ↓
CREATE_DEFENSE
```

---

## 34. Critérios de aceitação — descoberta

1. ADE descobre capabilities do PPG-ADM em runtime.
2. Nova capability não exige rebuild do frontend/backend do ADE.
3. Inputs obrigatórios vêm do contrato publicado.
4. Outputs vêm do contrato publicado.
5. `$ref` é resolvido.
6. arrays e objetos são suportados.
7. contrato normalizado não depende da UI.
8. refresh detecta mudanças.

---

## 35. Critérios de aceitação — Wizard

Para `CREATE_DEFENSE`:

1. Wizard mostra automaticamente os inputs atuais.
2. `studentId`, `advisorId`, `title`, `date`, `location` e `committeeMembers` não estão hardcoded.
3. tipo e obrigatoriedade são apresentados.
4. Wizard informa que os requisitos vieram do contrato do PPG-ADM.
5. identifica dados já existentes.
6. identifica dados ausentes.
7. permite configurar suas origens.
8. distingue mapping, extraction, transformation e lookup.
9. não apresenta JSON/expressões como interface principal.
10. atualiza o `ProcessDataContext`.

---

## 36. Critérios de aceitação — GMS/CIR

1. selecionar EMAIL disponibiliza o schema real do canal;
2. campos de envelope continuam disponíveis;
3. dados internos do body são configuráveis por extraction contract;
4. CIR recebe/atualiza configuração sem rebuild;
5. payload estruturado chega ao processo;
6. tipos são validados;
7. campo obrigatório ausente produz diagnóstico compreensível;
8. arrays/objetos funcionam para `committeeMembers`.

---

## 37. Teste arquitetural obrigatório — nova capability desconhecida

Criar uma nova operação apenas no provider/fixture OpenAPI, com parâmetros diferentes dos processos existentes.

Sem alterar código do Wizard:

1. publicar/alterar o contrato;
2. executar refresh;
3. capability aparecer no Registry;
4. inputs aparecerem no Wizard;
5. usuário conseguir configurar suas origens;
6. nenhuma recompilação do ADE ser necessária.

Esse teste comprova o objetivo principal da sprint.

---

## 38. Outros testes obrigatórios

### Teste A — Cadastro de Defesa

```text
studentId:Long
advisorId:Long
title:String
date:Date
location:String
committeeMembers:Array<CommitteeMember>
```

Validar discovery, resolução, extração e readiness.

### Teste B — Mudança de contrato

Adicionar novo campo obrigatório e detectar configuração desatualizada.

### Teste C — Schema complexo

Validar array de objetos e `$ref`.

### Teste D — Provider indisponível

Validar cache/fallback e diagnóstico.

### Teste E — incompatibilidade

`body:String` não pode ser tratado automaticamente como `studentId:Long`.

### Teste F — correção reativa

Depois de configurar a origem de um dado faltante, o erro deve desaparecer imediatamente e o readiness deve ser recalculado.

---

## 39. Não fazer

Não:

- hardcodar `CREATE_DEFENSE`;
- hardcodar seus campos;
- modificar o Wizard a cada endpoint novo;
- fazer o frontend consumir OpenAPI diretamente;
- tratar todo input como String;
- confundir mapping com extraction;
- assumir que IDs estão no e-mail;
- gerar dados inexistentes;
- permitir mapping incompatível apenas para satisfazer validação;
- expor JSON técnico ao usuário comum;
- colocar parsing específico de Defesa dentro do GMS;
- reconstruir ADE para atualizar contratos;
- usar IA generativa como requisito desta sprint.

---

## 40. Documentação

Criar:

```text
docs/e09/runtime-capability-discovery.md
docs/e09/capability-contract-model.md
docs/e09/process-input-requirements.md
docs/e09/data-resolution-plan.md
docs/e09/input-extraction-contract.md
docs/e09/cir-extraction-integration.md
docs/e09/schema-evolution.md
```

---

## 41. Relatório

Criar:

```text
docs/sprints/E09-report.md
```

Incluir:

- arquitetura anterior e nova;
- mecanismo real de metadata/OpenAPI do PPG-ADM;
- contrato real de `CREATE_DEFENSE`;
- schema real de `CommitteeMember`;
- screenshots do Wizard;
- `DataResolutionPlan`;
- configuração enviada ao CIR;
- exemplo de e-mail/template;
- payload extraído;
- `ProcessDataContext`;
- teste de capability nova sem rebuild;
- teste de evolução de contrato;
- limitações.

---

## 42. Resultado esperado

Antes:

```text
Endpoint novo
    ↓
programador altera ADE
    ↓
rebuild
    ↓
Wizard conhece os campos
```

Depois:

```text
Endpoint novo
    ↓
provider publica contrato
    ↓
ADE descobre em runtime
    ↓
Capability Registry normaliza
    ↓
Wizard identifica requisitos
    ↓
usuário define as origens
    ↓
CIR recebe contrato de extração
    ↓
processo recebe dados estruturados
```

Princípio final:

> **O ADE não deve conhecer previamente os dados de cada funcionalidade do sistema de domínio. Ele deve descobrir o contrato da funcionalidade em tempo de execução, transformar seus parâmetros em requisitos de dados da automação e orientar o usuário na configuração de como esses dados serão obtidos, transformados ou consultados.**

O processo **Cadastro de Defesa** comprova a solução, mas não define sua implementação.
