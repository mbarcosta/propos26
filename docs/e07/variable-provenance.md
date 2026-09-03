# E07 - Variable Provenance

O Wizard apresenta origem e tipo dos dados disponiveis para evitar que o usuario configure mappings sem entender de onde os valores vem.

Exemplo visual:

```text
requestId       String   Receber solicitacao
requesterEmail  String   Receber solicitacao
dadosCompletos Boolean  Verificar dados
```

## Origem

Para dados recebidos em mensagens, a origem e o nome do elemento inbound ou start event.

Para dados produzidos por capability, a origem e o nome da Service Task que executa a capability.

## Disponibilidade

A disponibilidade e sensivel a posicao no processo analisado pelo Wizard. Uma variavel de uma etapa futura nao aparece em capability inputs, e-mail, correlacao ou gateway anteriores.

## Uso

O mesmo picker de variaveis e usado em:

- inputs de capabilities;
- correlacao de mensagens;
- destinatario de e-mail;
- condicoes de gateways;
- campos com escolha entre dado do processo e valor fixo.
