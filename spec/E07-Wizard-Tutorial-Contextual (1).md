# E07 — Wizard Tutorial, Contextual e Orientado à Intenção

## 1. Objetivo

Evoluir o Automation Configuration Wizard do ADE. A estrutura modal e a navegação criadas no E06 devem ser preservadas, mas o Wizard deve deixar de ser apenas um sequenciador das propriedades técnicas da tela Automation e tornar-se um assistente tutorial, contextual e orientado à intenção.

O princípio de interação será:

EXPLICAR → PERGUNTAR → SUGERIR → USUÁRIO ESCOLHE → VALIDAR → MOSTRAR CONSEQUÊNCIA.

Não utilizar IA generativa nesta sprint. Sugestões devem ser determinísticas, baseadas no BPMN, contratos das capabilities, tipos, variáveis, posição no fluxo e configuração existente.

## 2. Idioma e linguagem

O Wizard desta distribuição do ADE deverá usar português como idioma principal. Identificadores técnicos, nomes de capabilities e variáveis permanecem inalterados.

Não basta traduzir labels. Por exemplo, não substituir apenas `Correlation Key` por `Chave de Correlação`. O conceito deve ser explicado:

> **Como o ADE reconhecerá a resposta correta?**
>
> Durante a execução podem existir várias solicitações aguardando respostas. O sistema precisa de uma informação que permita descobrir a qual solicitação cada resposta pertence.
>
> **Identificador da solicitação**
> `[ requestId ▼ ]`
>
> Termo técnico: `Correlation Key`

Detalhes técnicos deverão ficar em uma área secundária `Ver detalhes técnicos`.

## 3. Estrutura pedagógica das etapas

Cada etapa deverá, quando aplicável:

1. apresentar título orientado à tarefa;
2. explicar o papel do elemento no processo;
3. explicar o que precisa ser configurado;
4. explicar por que a informação é necessária;
5. mostrar as opções adequadas;
6. sugerir valores quando houver regra determinística segura;
7. validar a escolha;
8. resumir o que acontecerá durante a execução.

Exemplo para o Start Event:

### Configuração do início do processo — Receber solicitação

> Este evento inicia o processo quando uma solicitação externa é recebida. Vamos definir como ela chega, como será reconhecida e quais informações ficarão disponíveis para as próximas etapas.

Informações como `startEvent`, `Start_Vinculacao`, requirements e IDs devem ficar como detalhes técnicos.

## 4. Progressive Disclosure

A visualização padrão será a configuração guiada.

Adicionar uma área recolhível:

`▸ Ver detalhes técnicos`

que poderá mostrar BPMN Type, BPMN ID, Automation Requirements, Capability ID, provider, endpoint, router, Message Name, implementation type e expressões geradas.

Não remover a transparência técnica; apenas deixar de torná-la a linguagem dominante.

## 5. Process Data Context — requisito arquitetural central

Introduzir/consolidar um mecanismo conceitualmente chamado `ProcessDataContext`.

Sua responsabilidade é responder:

> Quais dados estão disponíveis antes da execução deste elemento BPMN?

Cada variável deverá possuir, quando possível:

- name;
- type;
- origin;
- producerElement;
- description;
- availability.

Exemplo:

| Dado | Tipo | Origem |
|---|---|---|
| `requestId` | String | Receber solicitação |
| `requesterEmail` | String | Receber solicitação |
| `advisorEmail` | String | Receber solicitação |
| `dadosCompletos` | Boolean | Verificar dados |

As variáveis devem ser sensíveis à posição no grafo. Uma variável criada depois de um gateway não pode ser sugerida como condição desse gateway.

## 6. Propagação de dados

As variáveis configuradas no evento inicial, incluindo casos como `requesterEmail` e `advisorEmail`, deverão entrar no ProcessDataContext e ficar disponíveis para elementos posteriores.

Outputs das capabilities também deverão alimentar o contexto.

Exemplo:

`Verificar dados → dadosCompletos:Boolean → Gateway Dados completos?`

O gateway deverá reconhecer `dadosCompletos` automaticamente como variável disponível.

Não hardcodar nomes como requesterEmail ou advisorEmail; eles são casos de teste da infraestrutura genérica de propagação.

## 7. Variable Picker

Criar componente reutilizável `ProcessVariablePicker` ou equivalente.

Mostrar:

- nome;
- tipo;
- origem;
- descrição quando disponível.

Permitir filtro por tipo e priorizar variáveis adequadas ao campo.

Usar o componente em capability inputs, correlação, e-mail, gateways e mappings.

Quando um campo aceitar literal ou variável:

**Fonte do valor**
- Dado do processo
- Valor fixo

O usuário não deve precisar digitar `${...}` nas configurações comuns. O ADE pode gerar a expressão técnica internamente.

## 8. Evento inicial

Para `Receber solicitação`, apresentar inicialmente:

### Configuração do início do processo

> Este processo começa quando uma solicitação externa é recebida. Precisamos definir de onde ela chega e como será transformada em uma nova instância do processo.

### Como a solicitação chega?

`Canal de recebimento [ E-mail ▼ ]`

Explicar que, nesta configuração, o GMS recebe o e-mail e o CIR encaminha o evento ao processo.

Depois perguntar:

### Qual evento externo inicia este processo?

> Informe um identificador claro e estável que represente a ocorrência reconhecida pelo CIR.

Exemplo: `SOLICITACAO_VINCULACAO_RECEBIDA`.

### Como uma resposta será associada à solicitação correta?

Explicar o problema de múltiplas instâncias e permitir selecionar um identificador como `requestId`.

### Quais dados ficarão disponíveis?

Apresentar os initial mappings em linguagem de dados, com nome, tipo e origem.

## 9. Unicidade e ambiguidade

Antes de implementar alertas, inspecionar a semântica real do CIR e do Camunda existentes.

Não afirmar que o Camunda exige globalmente nomes de mensagem únicos sem verificar.

Validar as ambiguidades que realmente possam ocorrer no propos26. Se um evento externo estiver associado de forma conflitante a mais de um processo, explicar o risco e indicar os processos/configurações envolvidos.

## 10. Service Task

Para `Verificar dados`, iniciar:

### Configuração da tarefa "Verificar dados"

> Esta é uma tarefa automatizada. Precisamos definir qual funcionalidade disponível no ambiente realizará esse trabalho.

### O que esta tarefa deve fazer?

Não mostrar inicialmente a lista completa de dezenas de capabilities.

Mostrar primeiro `Funcionalidades compatíveis` ou `Recomendadas`, calculadas deterministicamente por requirements, tipos e contratos.

Oferecer `Mostrar todas as capabilities` e busca como alternativa.

Ao selecionar `FIND_PROFESSOR`, por exemplo:

### Buscar professor

> Esta funcionalidade consulta o PPG Management e retorna os dados de um professor cadastrado.

**O que ela precisa?**
Identificador do professor.

**O que ela devolve?**
ID, nome e e-mail.

Somente em detalhes técnicos mostrar REST, endpoint, provider e implementation.

## 11. Input Mapping tutorial

Em vez de:

`professorId Long <-`

mostrar:

### Qual dado do processo será usado como identificador do professor?

Esperado: `professorId (Long)`

`Usar: [ variável disponível ▼ ]`

Mostrar origem da opção escolhida.

## 12. Output Mapping tutorial

Em vez de:

`id Long ->`
`name String ->`
`email String ->`

mostrar:

### Quais resultados você deseja disponibilizar para as próximas etapas?

- ID do professor — salvar como `[ professorId ]`
- Nome — salvar como `[ professorName ]`
- E-mail — salvar como `[ professorEmail ]`

Explicar que esses dados ficarão disponíveis posteriormente.

## 13. Exclusive Gateway — requisito crítico

Para `Dados completos?`:

### Configuração da decisão "Dados completos?"

> Este ponto decide qual caminho o processo seguirá. Escolha um dado produzido anteriormente e defina quando cada caminho deve ser utilizado.

Mostrar:

### Dados disponíveis para esta decisão

`dadosCompletos : Boolean — produzido por "Verificar dados"`
`requesterEmail : String — recebido em "Receber solicitação"`
`advisorEmail : String — recebido em "Receber solicitação"`

Priorizar variáveis tipologicamente adequadas. Uma variável Boolean deve aparecer antes de Strings para uma decisão booleana.

## 14. Editor visual de condições

Não exigir inicialmente expressão Camunda manual.

Exemplo:

**Caminho: Dados Sim**

`Quando [ dadosCompletos ▼ ] [ for ▼ ] [ verdadeiro ▼ ]`

**Caminho: Dados Não**

`Quando [ dadosCompletos ▼ ] [ for ▼ ] [ falso ▼ ]`

O ADE deverá gerar a expressão técnica apropriada, respeitando a tecnologia de expressão efetivamente usada pelo Camunda 7 do projeto.

Implementar conjunto inicial de operadores por tipo:

Boolean:
- é verdadeiro;
- é falso;
- é igual a.

String:
- é igual a;
- é diferente de;
- está vazio;
- não está vazio.

Number:
- igual;
- maior;
- menor;
- maior ou igual;
- menor ou igual.

Não é necessário implementar linguagem completa.

## 15. Default Flow

Explicar:

### Caminho padrão

> Será usado quando nenhuma das condições anteriores for satisfeita.

Usar controle compreensível, por exemplo:

`☐ Usar este caminho como padrão`

Não mostrar apenas `Default branch`.

Validar fluxos sem condição, múltiplos defaults, variáveis indisponíveis, incompatibilidades simples de tipo e condições evidentemente duplicadas.

## 16. Relação tarefa → gateway

Quando um gateway ocorrer após uma Service Task, destacar outputs adequados da tarefa:

> A tarefa anterior "Verificar dados" produz `dadosCompletos:Boolean`. Este resultado pode ser usado para configurar esta decisão.

Isso é uma inferência estrutural/tipológica determinística, não IA.

## 17. E-mail

Para `SEND_EMAIL`, usar:

### Envio de e-mail

> Esta tarefa enviará uma mensagem durante a execução do processo.

Campos principais:

- Destinatário;
- Assunto;
- Mensagem.

Para destinatário, oferecer variáveis disponíveis como `requesterEmail`, `advisorEmail` etc., quando realmente existirem naquele ponto.

Mostrar nome, tipo e origem.

Permitir variável ou valor fixo.

Termos como `mail-to`, `subject` e `body` ficam em detalhes técnicos.

## 18. Message Catch Event / Receive Task

Explicar primeiro:

> Neste ponto o processo ficará aguardando uma mensagem. Precisamos definir qual mensagem é esperada e como o ADE identificará a instância correta quando ela chegar.

Guiar o usuário por:

1. tipo/canal da mensagem;
2. evento externo;
3. nome da mensagem no processo;
4. identificador usado para localizar a instância;
5. dados extraídos da mensagem.

Explicar Message Name e correlação antes de mostrar os campos técnicos.

## 19. Diagnóstico

Substituir como linguagem principal:

`INFO: ID defined...`
`INFO: No blocking issues detected.`

por:

`✓ O elemento possui nome e identificador válidos.`
`✓ Esta etapa está configurada corretamente.`

Adicionar `Ver diagnóstico técnico` para os detalhes.

Erros devem ser acionáveis:

`O dado obrigatório studentId ainda não foi associado a nenhum dado do processo.`

## 20. Resumo de execução

Cada etapa relevante deverá poder mostrar:

### O que acontecerá durante a execução

Exemplo Service Task:

1. o valor de `professorId` será lido;
2. o PPG Management será consultado;
3. nome e e-mail serão retornados;
4. esses dados ficarão disponíveis para as próximas etapas.

Evento inicial:

1. GMS recebe a mensagem;
2. CIR reconhece o evento;
3. nova instância é iniciada;
4. variáveis iniciais ficam disponíveis.

Gateway:

- `dadosCompletos = verdadeiro → Dados Sim`;
- `dadosCompletos = falso → Dados Não`.

Gerar os resumos deterministicamente a partir da configuração.

## 21. Validação final

A tela final deverá usar linguagem tutorial:

### Processo pronto para implantação

- ✓ O início do processo está configurado.
- ✓ Todas as tarefas automatizadas possuem funcionalidades associadas.
- ✓ Os dados obrigatórios estão mapeados.
- ✓ As decisões possuem condições válidas.
- ✓ As mensagens possuem regras de correlação.
- ✓ Serviços necessários estão acessíveis.

Quando houver erros:

### 2 configurações precisam de atenção

1. **Dados completos?** — o caminho "Dados Sim" não possui condição. `[ Revisar ]`
2. **Enviar confirmação** — nenhum destinatário foi definido. `[ Revisar ]`

Manter `READY_FOR_DEPLOYMENT` apenas como estado técnico secundário.

## 22. Arquitetura

Avaliar introdução/consolidação de:

- ProcessDataContext;
- ProcessVariableDescriptor;
- VariableOrigin;
- ProcessDataFlowAnalyzer;
- WizardContentProvider;
- WizardHelpContent;
- CapabilityCompatibilityResolver;
- GatewayConditionBuilder;
- ProcessVariablePicker;
- ExecutionSummaryBuilder.

Evitar lógica contextual espalhada diretamente pelos componentes de UI.

Separar conteúdo tutorial da implementação visual quando apropriado, preparando futura internacionalização pt-BR/en sem exigir troca de idioma nesta sprint.

## 23. Sincronização

Preservar:

`Wizard ↔ AutomationProject ↔ Automation/Capabilities/Integrations`

Não criar segundo modelo de configuração.

A interface técnica existente continua disponível para usuários avançados.

## 24. Casos de teste obrigatórios

### Vinculação de Orientação

Validar especialmente:

**Receber solicitação**
- explicação de canal;
- evento externo;
- mensagem;
- correlação;
- initial mappings;
- requesterEmail/advisorEmail disponíveis posteriormente.

**Verificar dados**
- capability contextual;
- inputs e origem;
- outputs e propagação.

**Dados completos?**
- dados disponíveis;
- output da tarefa anterior;
- condição visual;
- default flow;
- resumo dos caminhos.

### Cancelamento de Orientação

Repetir para comprovar genericidade.

Não hardcodar qualquer processo.

## 25. Testes automatizados

Adicionar testes para:

- propagação de variável do Start Event;
- propagação de output de Service Task;
- não propagação de variável futura;
- gateway recebendo variável anterior;
- filtro/priorização por tipo;
- criação de condição booleana;
- geração de expressão técnica;
- persistência;
- reabertura;
- sincronização com AutomationProject;
- variável de e-mail disponível quando originada anteriormente.

## 26. Restrições

Não:

- reimplementar a estrutura modal do E06;
- criar outro Wizard;
- duplicar AutomationProject;
- usar IA generativa;
- hardcodar Vinculação ou Cancelamento;
- hardcodar requesterEmail/advisorEmail como regras globais;
- afirmar regras de unicidade Camunda/CIR sem inspecionar a implementação;
- exigir `${...}` em operações comuns;
- transformar a sprint em mera tradução de labels.

## 27. Estratégia para o Codex

### Fase 1 — Inspeção

Antes de modificar código:

1. revisar E06;
2. localizar initial mappings;
3. localizar outputs de capabilities;
4. analisar o grafo BPMN;
5. analisar sequence flow conditions;
6. analisar GMS/CIR e a semântica real de eventos/correlação;
7. descobrir por que requesterEmail/advisorEmail não aparecem atualmente.

### Fase 2 — Process Data Context

Implementar e testar primeiro o contexto de dados. Não começar apenas pela alteração dos textos.

### Fase 3 — Componentes reutilizáveis

Criar Variable Picker, ajuda contextual, detalhes técnicos, guided mapping e condition builder.

### Fase 4 — Evento inicial

Reformular a experiência.

### Fase 5 — Service Task

Reformular capability selection e mappings.

### Fase 6 — Gateway

Implementar condition builder consciente dos dados disponíveis.

### Fase 7 — E-mail e mensagens

Usar ProcessDataContext.

### Fase 8 — Validação final

Transformar diagnóstico técnico em orientação acionável.

### Fase 9 — E2E

Executar Vinculação e Cancelamento de Orientação.

## 28. Critérios de aceitação

A sprint somente será concluída quando:

1. português for a linguagem principal do Wizard;
2. conceitos técnicos forem explicados, não apenas traduzidos;
3. detalhes técnicos estiverem disponíveis secundariamente;
4. cada etapa explicar propósito, decisão e consequência;
5. ProcessDataContext estiver implementado;
6. variáveis iniciais forem propagadas;
7. outputs de capabilities forem propagados;
8. origem e tipo forem apresentados quando conhecidos;
9. variáveis futuras não forem sugeridas;
10. requesterEmail e advisorEmail aparecerem quando válidos;
11. capabilities compatíveis forem apresentadas antes do catálogo completo;
12. inputs e outputs tiverem configuração tutorial;
13. gateway listar dados disponíveis;
14. gateway permitir montar condições visualmente;
15. expressão técnica for gerada pelo ADE;
16. default flow for explicado;
17. campos de e-mail puderem selecionar variáveis disponíveis;
18. mensagens/correlação forem explicadas em linguagem orientada ao problema;
19. validação final apresentar erros acionáveis;
20. Vinculação e Cancelamento funcionarem sem código específico.

## 29. Documentação e relatório

Criar:

- `docs/e07/tutorial-wizard.md`
- `docs/e07/process-data-context.md`
- `docs/e07/variable-provenance.md`
- `docs/e07/gateway-condition-builder.md`
- `docs/e07/contextual-capabilities.md`
- `docs/e07/message-and-correlation-guidance.md`
- `docs/sprints/E07-report.md`

O relatório deve incluir comparação antes/depois, screenshots, ProcessDataContext, regras de propagação, exemplos do Start Event, Service Task e Gateway, testes, limitações e recomendações.

## 30. Resultado esperado

A experiência final para o gateway deve se aproximar de:

### Configuração da decisão "Dados completos?"

Este ponto decide qual caminho o processo seguirá.

A tarefa anterior **Verificar dados** produziu:

`dadosCompletos : Boolean`

Você pode utilizar esse resultado para tomar a decisão.

**Caminho "Dados Sim"**

`Quando [ dadosCompletos ] [ for ] [ verdadeiro ]`

**Caminho "Dados Não"**

`Quando [ dadosCompletos ] [ for ] [ falso ]`

✓ Os dois caminhos estão configurados corretamente.

**O que acontecerá**
- verdadeiro → Dados Sim
- falso → Dados Não

`▸ Ver detalhes técnicos`

O Wizard deverá deixar de ser apenas um **sequenciador de propriedades técnicas** e passar a funcionar efetivamente como um **assistente que ensina, orienta, contextualiza, configura e valida a automação do processo**.
