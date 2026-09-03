# E07 - Wizard Tutorial, Contextual e Orientado a Intencao

## Resumo

O Wizard do ADE foi evoluido de um sequenciador tecnico para um assistente tutorial em portugues, preservando a estrutura modal do E06. A configuracao continua editando `AutomationProject`, bindings, mappings, inbound/outbound, correlacoes e condicoes existentes.

## Antes / Depois

Antes: o Wizard apresentava principalmente propriedades tecnicas de Automation, capabilities, mappings e gateways.

Depois: cada etapa explica o papel do elemento BPMN, pergunta a intencao de configuracao, mostra sugestoes deterministicas, valida escolhas e apresenta o que acontecera durante a execucao.

## Implementacao

Arquivos principais:

- `platform/automation-development-environment/src/main/resources/static/app.js`;
- `platform/automation-development-environment/src/main/resources/static/index.html`.

Foram adicionados:

- linguagem principal em portugues no Wizard;
- `ProcessDataContext`;
- descritores de variaveis com tipo, origem e produtor;
- picker reutilizavel de variaveis;
- escolha entre dado do processo e valor fixo;
- capability picker contextual;
- mapeamento tutorial de inputs e outputs;
- builder visual de condicoes para Exclusive Gateway;
- resumo de execucao por etapa;
- validacao final com mensagens acionaveis;
- detalhes tecnicos recolhaveis.

## ProcessDataContext

O contexto propaga dados recebidos em Start/Message events e outputs mapeados de capabilities. Variaveis futuras nao sao sugeridas, pois o contexto e calculado apenas com etapas anteriores ao elemento atual.

`requesterEmail` e `advisorEmail` aparecem quando definidos nos mappings do evento inicial, sem regra especial para esses nomes.

Para BPMNs legados, o ADE importa `camunda:topic` como binding de capability quando houver equivalencia no registry. O topico antigo `VALIDATE_ADVISORSHIP_REQUEST` e tratado como alias de `CHECK_ADVISORSHIP`, e `REGISTER_ADVISORSHIP` como alias de `CREATE_ADVISORSHIP`.

No processo de Vinculacao de Orientacao, isso faz a tarefa `Verificar dados` abrir com `CHECK_ADVISORSHIP`, inputs `studentId` e `advisorId`, e outputs `valid`/`reason`. Quando a proxima etapa e um gateway como `Dados completos?`, o output booleano recebe sugestao de nome `dadosCompletos`.

## Gateway

O gateway lista dados disponiveis antes da decisao e prioriza variaveis booleanas. O editor visual gera expressoes Camunda 7 como:

```text
${dadosCompletos == true}
${dadosCompletos == false}
```

O caminho padrao e configurado por checkbox e explicado em linguagem de processo.

## Mensagens e Correlacao

Foram inspecionados CIR e deployment do ADE. As rotas usam `externalEvent`, `messageName`, `processDefinitionKey`, `businessKeyVariable`, `correlationVariable` e `subjectContains`.

O Wizard valida campos obrigatorios e evita afirmar regras globais de unicidade Camunda que nao foram comprovadas na implementacao.

## Screenshots

Screenshots nao foram capturados nesta execucao porque as aplicacoes nao foram iniciadas pelo Codex; o usuario controla o compose. Pontos a registrar manualmente:

- modal do Wizard com backdrop;
- etapa de evento inicial;
- etapa de Service Task com capabilities recomendadas;
- etapa de Gateway com builder visual;
- validacao final.

## Testes

Verificacoes executadas:

- sintaxe JavaScript com `node --check`;
- compilacao Maven sem testes.

Testes automatizados de UI/E2E ainda devem ser adicionados para Playwright ou ferramenta equivalente quando o ambiente de compose estiver em execucao.

## Limitacoes

O contexto segue a sequencia de etapas analisada pelo Wizard e ainda nao implementa uma analise completa de todos os caminhos possiveis do grafo BPMN. A inferencia de tipos para variaveis inbound e simples e deterministica.

## Recomendacoes para E08

- mover `ProcessDataContext` para modulo testavel fora do arquivo principal de UI;
- adicionar testes unitarios de propagacao e condicoes;
- validar conflitos reais de rotas CIR no backend;
- capturar screenshots automatizados do Wizard;
- evoluir inferencia de tipos com metadados declarados pelo usuario.
