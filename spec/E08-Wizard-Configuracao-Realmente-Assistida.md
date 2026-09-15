# E08 — Reestruturação do Automation Configuration Wizard: Configuração Realmente Assistida

## Objetivo

Reestruture o código e a experiência do **Automation Configuration Wizard** do ADE. O Wizard atual já possui a estrutura visual adequada, mas ainda exige do usuário conhecimento excessivo sobre a implementação técnica da automação.

A mudança fundamental é:

> **O usuário deve tomar decisões compreensíveis sobre o comportamento da automação. O Wizard é responsável por traduzir essas decisões para configurações técnicas de Camunda, CIR, GMS, capabilities, workers, expressions e variable mappings.**

O Wizard deve evitar perguntar diretamente por configurações que possam ser descobertas no BPMN, obtidas do Capability Registry/CIR/GMS, derivadas dos contratos, inferidas deterministicamente ou geradas automaticamente.

## 1. Regra fundamental

Para cada elemento BPMN, determinar antes de montar a UI:

1. O que o usuário realmente precisa decidir?
2. O que o ADE já sabe?
3. O que pode ser consultado?
4. O que pode ser gerado?
5. O que precisa apenas ser mostrado?
6. O que realmente precisa ser perguntado?

Aplicar:

`DECISÃO DO USUÁRIO → WIZARD → CONFIGURAÇÃO TÉCNICA`

e não:

`CONFIGURAÇÃO TÉCNICA → USUÁRIO PRECISA ENTENDER E PREENCHER`.

## 2. Configurador, não formulário técnico explicado

Não basta traduzir `Correlation Key` para `Chave de correlação`. Se `${requestId}` decorre de uma escolha compreensível do usuário, a expressão deve ser gerada internamente. Configurações técnicas derivadas devem ficar em `▸ Detalhes técnicos`.

## 3. Status da etapa

Mover o status para a faixa superior do modal, com estados como `NÃO CONFIGURADO`, `EM CONFIGURAÇÃO`, `ATENÇÃO`, `ERRO` e `CONFIGURADO`. Não colocar “OK: esta etapa está configurada corretamente” no meio do formulário.

## 4. Validação reativa — obrigatório

Corrigir o defeito em que erros permanecem depois da correção. A validação deve ser derivada do estado atual:

`configuration changed → recalculate derived configuration → validate current state → replace validation result → update UI`.

Não acumular mensagens históricas. Adicionar testes `invalid → correction → valid`.

## 5. Caso de referência: Start Event “Receber solicitação”

A etapa deve começar:

### Configuração do início do processo — Receber solicitação

> Para uma nova execução do processo iniciar, este evento deve ser identificado pelo sistema de automação.
>
> Para configurar essa captura é preciso:
> **I.** Associar o evento a um canal e a um tipo de entrada.
> **II.** Definir quais dados recebidos com o evento serão disponibilizados para utilização pelo processo.

Não começar mostrando `INBOUND_EVENT`, `MESSAGE_DEFINITION` ou `CORRELATION_DEFINITION`.

## 6. Recebimento da solicitação

Apresentar:

### Configurando o recebimento da solicitação

> Selecione uma das fontes de entrada disponíveis para este ambiente.

`Fonte da solicitação [ E-mail ▼ ]`

A escolha `E-mail` deve permitir ao Wizard derivar `GMS → CIR → Camunda`. Provider e Router não devem ser preenchidos manualmente quando decorrem do canal.

## 7. External Event e BPMN Message

Antes de mudar a UX, inspecionar CIR, GMS, ADE e BPMN e documentar para `External Event` e `Camunda Message Name`: quem define, onde é registrado, quem produz, quem consome, se precisa existir previamente, se pode ser criado pelo ADE e como é usado.

Não inventar a semântica.

Se o evento externo precisa existir no CIR/GMS, listá-lo em listbox. Se o ADE pode criá-lo, oferecer explicitamente `Usar evento existente` ou `Criar novo tipo de evento`, explicando que o novo evento será registrado na configuração apropriada.

Se o Message Start Event já referencia um objeto BPMN `Message`, apresentar mensagens existentes no modelo e selecionar automaticamente quando houver apenas uma opção válida.

Explicar:

- **Evento externo**: ocorrência reconhecida pela infraestrutura.
- **Mensagem BPMN**: representação da ocorrência usada pelo Camunda/modelo.

## 8. Não configurar correlação de instância no Start Event

Em um Message Start Event:

`mensagem chega → nova instância é criada`.

Ainda não existe instância anterior a localizar. Portanto, não apresentar “Como o ADE reconhecerá a resposta correta?” como correlação de uma instância existente. Essa lógica pertence principalmente aos Message Catch Events/Receive Tasks posteriores.

## 9. Identificador da solicitação

Se `requestId` for necessário para correlações futuras, explicar sua finalidade. Determinar no código se ele é recebido, gerado pelo CIR, ADE/worker, Camunda ou derivado de outro identificador. Não inventar.

Se não houver decisão do usuário:

`Identificador da solicitação: requestId — será criado automaticamente.`

Não mostrar `${requestId}` como campo editável.

## 10. Dados recebidos

Eliminar JSON/expressões como interface principal. Para `Canal = E-mail`, obter o schema real do GMS/CIR e permitir selecionar atributos:

- Remetente → `requesterEmail`
- Assunto → `subject`
- Corpo → `body`
- Destinatários → `recipients`
- Cópia → `cc`
- Anexos → `attachments`
- Identificador da mensagem → `messageId`

Usar somente campos realmente suportados e verificar attachments.

Generalizar:

`Input Channel + Event Type → Available Input Schema → User selects fields → Process Variables`.

Criar abstrações como `InboundDataSchema` e `InboundDataField`, sem hardcode específico de e-mail.

## 11. Dados produzidos

Depois da seleção, mostrar claramente os dados produzidos, seus tipos e origem, incluindo o identificador gerado automaticamente quando aplicável. Para o Start Event, “dados disponíveis antes desta etapa” pode simplesmente informar que não há dados anteriores.

## 12. Resumo do Start Event

Não usar “uma nova instância será iniciada ou uma mensagem será correlacionada”. Para Start Event, gerar resumo inequívoco:

1. GMS recebe o e-mail.
2. CIR identifica o tipo selecionado.
3. O evento inicia nova instância de Vinculação de Orientação.
4. Os campos selecionados são convertidos em dados do processo.
5. O identificador fica disponível para correlações futuras.

## 13. Aplicar o princípio a todos os elementos

Revisar Service Task, Send Task, Message Catch Event, Receive Task, Exclusive Gateway, Intermediate Events e demais elementos suportados. Pergunta central: “o usuário está configurando o comportamento ou detalhes internos da infraestrutura?”

## 14. Service Task “Verificar dados”

Usar como segundo caso obrigatório. A UI deve eliminar a incerteza sobre quem interpreta `body`, de onde vêm `studentId` e `advisorId`, o que a capability recebe e devolve e de onde vem `dadosCompletos`.

Ao selecionar uma capability, apresentar:

- O que esta funcionalidade faz?
- O que precisa receber?
- O que produzirá?

Se `dadosCompletos` for retorno da capability, mostrar isso explicitamente e inserir o output no `ProcessDataContext`.

## 15. Inputs e compatibilidade

Para cada input, perguntar semanticamente de onde vem o dado e mostrar apenas opções compatíveis disponíveis naquele ponto.

Não sugerir `body:String → studentId:Long` apenas porque `body` existe. Se não há dado compatível, explicar:

> A capability precisa de `studentId (Long)`, mas nenhum dado desse tipo está disponível. `body (String)` é o texto do e-mail e não pode ser usado diretamente como identificador. É necessária uma etapa/capability anterior que extraia ou identifique o estudante.

## 16. Mapping ≠ Transformation

Formalizar:

- **Mapping**: associa dado existente a parâmetro compatível.
- **Transformation/Extraction**: produz novo dado a partir de outro.

`body:String` não se transforma em `studentId:Long` por mapping. Se faltar uma capability de extração, identificar/documentar o gap; não criar arbitrariamente uma capability sem verificar a arquitetura.

## 17. Outputs e ProcessDataContext

Outputs como `studentId`, `advisorId` e `dadosCompletos` devem entrar automaticamente no contexto e ficar disponíveis para elementos posteriores, com tipo e origem.

## 18. Gateway “Dados completos?”

A etapa deve dizer que ocorre depois de `Verificar dados` e mostrar `dadosCompletos:Boolean` como resultado anterior adequado à decisão.

Permitir:

- `dadosCompletos = verdadeiro → caminho Dados completos`
- `dadosCompletos = falso → caminho Dados incompletos`

Gerar internamente a expressão Camunda. Não exigir `${dadosCompletos == true}`.

## 19. Message Catch Event — correlação pertence aqui

Para “Aguardar confirmação do estudante”, explicar que já existe uma instância aguardando e que o CIR precisa identificar qual instância receberá a resposta.

Permitir selecionar `requestId`, mostrando sua origem em “Receber solicitação”. Quando aplicável, visualizar a cadeia:

`requestId → mensagem enviada → resposta → CIR → instância correta`.

## 20. Send Task

Perguntar em termos de intenção:

- Para quem enviar?
- Qual assunto?
- Qual mensagem?
- É necessário incluir identificador para futura resposta?
- Há documento/link a incluir?

Se uma resposta posterior depender de `requestId`, alertar que a mensagem precisa permitir sua recuperação. Gerar a configuração técnica internamente.

## 21. Erros devem ensinar

Toda validação deve responder:

1. O que está errado?
2. Por que está errado?
3. Como corrigir?

Oferecer ações como `[ Revisar etapa anterior ]` quando possível.

## 22. Classificação das propriedades

Classificar propriedades como:

- `USER_DECISION`
- `DERIVED`
- `GENERATED`
- `DISCOVERED`
- `ADVANCED`

Exemplo:

- Canal → USER_DECISION
- Evento externo → USER_DECISION ou DISCOVERED
- Provider GMS → DERIVED
- Router CIR → DERIVED
- `${requestId}` → GENERATED
- BPMN ID → DISCOVERED
- endpoint REST → DISCOVERED

A UI principal deve concentrar-se em `USER_DECISION`.

## 23. Detalhes técnicos

Preservar `▸ Ver detalhes técnicos` para BPMN ID, Message ID, External Event ID, Provider, Router, Expression, Capability ID, endpoint REST, Worker e configuração Camunda.

## 24. Revisão semântica obrigatória antes da UX

Antes de alterar a interface, criar:

`docs/e08/wizard-configuration-semantics.md`

Documentar para cada conceito sua fonte, quem cria, quem usa e se o usuário precisa decidir. Incluir pelo menos: Inbound Channel, External Event, BPMN Message, requestId, correlationId, Initial Mapping, Capability Input e Capability Output.

Não inventar respostas: inspecionar o código.

## 25. Requirements são internos, intenção é a UX

Não renderizar simplesmente formulários para `INBOUND_EVENT`, `MESSAGE_DEFINITION`, `CORRELATION_DEFINITION`, `SERVICE_CAPABILITY` e `CONDITION_VALIDATION`.

`AutomationRequirement` continua útil internamente, mas a experiência deve ser composta pela intenção do elemento:

`Start Message Event → Configurar como o processo será iniciado`.

## 26. Pipeline geral

Implementar conceitualmente:

`BPMN Element → Determine Element Intent → Discover Existing Configuration → Discover Available Resources → Determine Required User Decisions → Build Guided Configuration → Generate Technical Configuration → Validate → Explain Runtime Behavior`.

## 27. Critérios de aceitação — Start Event

A etapa “Receber solicitação” só está concluída quando:

1. canal é escolhido sem configurar manualmente provider/router;
2. fica claro se evento externo é existente ou novo;
3. eventos existentes são listados quando aplicável;
4. diferença entre External Event e BPMN Message está clara;
5. objetos BPMN existentes são reutilizados;
6. correlação com instância existente não é solicitada indevidamente;
7. requestId tem origem/finalidade claras;
8. expressões técnicas são geradas;
9. campos do e-mail são selecionáveis visualmente;
10. attachments aparecem se suportados;
11. variáveis produzidas são mostradas;
12. resumo informa nova instância inequivocamente;
13. erros desaparecem após correção.

## 28. Critérios de aceitação — Verificar dados

1. propósito da capability explicado;
2. inputs apresentados semanticamente;
3. origem dos inputs clara;
4. tipos incompatíveis não sugeridos;
5. mapping e transformação distinguidos;
6. outputs claramente identificados;
7. fica explícito se `dadosCompletos` é retorno;
8. outputs entram no ProcessDataContext;
9. erros indicam causa e correção;
10. erros desaparecem quando corrigidos.

## 29. Demais etapas

Aplicar:

`explicar intenção → descobrir configuração → perguntar somente decisões necessárias → gerar detalhes técnicos → validar → explicar comportamento resultante`.

## 30. Resultado esperado

O Wizard não deve exigir conhecimento prévio de GMS, CIR, Camunda Message Name, Correlation Expression, REST endpoint, External Task topic, JSON mapping ou `${...}` para uma automação comum.

Essas informações continuam disponíveis nos detalhes técnicos.

> **Um usuário que compreende o processo de negócio deve conseguir configurar a automação sendo conduzido pelo Wizard, mesmo sem conhecer previamente os detalhes internos de Camunda, CIR, GMS ou da implementação dos workers.**

O Wizard deve atuar como **tradutor entre a intenção do usuário e a configuração técnica da automação**.
