# E07 - ProcessDataContext

`ProcessDataContext` e o mecanismo usado pelo Wizard para responder quais dados estao disponiveis antes da execucao de um elemento BPMN.

O contexto e calculado a partir da propria `AutomationProject`, sem criar um segundo modelo funcional. Ele le:

- configuracoes inbound/start;
- mappings de variaveis;
- outputs das capabilities vinculadas;
- ordem de etapas produzida pela analise BPMN do Wizard.

Cada variavel e representada conceitualmente por:

```text
ProcessVariableDescriptor
name
type
origin
producerElement
description
availability
```

## Regras de Propagacao

Dados configurados no evento inicial entram no contexto de elementos posteriores. Isso inclui qualquer variavel declarada nos initial mappings, como `requesterEmail`, `advisorEmail` ou outros nomes definidos pelo usuario.

Outputs de Service Tasks tambem entram no contexto quando mapeados. O nome salvo pelo usuario no output mapping e usado como variavel disponivel para etapas posteriores.

Variaveis produzidas depois do elemento atual nao sao sugeridas. A implementacao corta o contexto no indice da etapa atual na sequencia do Wizard.

## Tipos

Quando o tipo vem de contrato de capability, ele e normalizado. Quando vem de mapping inbound, o tipo e inferido por regras deterministicas de nome:

- nomes booleanos ou com `valid`, `complete`, `completo`, `aprovado` viram `Boolean`;
- nomes terminados em `id` ou relacionados a contagem viram `Long`;
- nomes com `file` viram `File`;
- demais nomes viram `String`.

Essa inferencia e propositalmente simples e testavel.
