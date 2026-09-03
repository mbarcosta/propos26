# E07 - Message And Correlation Guidance

O Wizard explica mensagens em linguagem orientada ao problema:

```text
Neste ponto o processo ficara aguardando uma mensagem.
Precisamos definir qual mensagem e esperada e como o ADE identificara a instancia correta quando ela chegar.
```

## Semantica Observada

A implementacao atual do CIR usa rotas com:

- `externalEvent`;
- `action`;
- `messageName`;
- `processDefinitionKey`;
- `businessKeyVariable`;
- `correlationVariable`;
- `subjectContains`.

Para inicio de processo, o ADE publica rota com `START_PROCESS`, `messageName`, `processDefinitionKey`, `businessKeyVariable` e `correlationVariable`.

Para mensagens intermediarias, publica rota com `CORRELATE_MESSAGE`, `messageName` e `correlationVariable`.

O cliente Camunda do CIR trata `messageName` como obrigatorio e usa correlation keys para mensagens intermediarias. A implementacao atual de correlacao do cliente usa `correlationId`.

## Validacao

O Wizard valida a presenca de:

- evento externo;
- nome de mensagem no processo;
- campo de correlacao.

Ele nao afirma unicidade global de mensagens Camunda. A validacao de ambiguidade deve considerar conflitos reais nas rotas CIR/ADE quando essa leitura for persistida no backend.
