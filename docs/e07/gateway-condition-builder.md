# E07 - Gateway Condition Builder

Exclusive Gateways agora podem ser configurados por um editor visual, sem exigir expressao Camunda manual como primeira interacao.

O Wizard lista os dados disponiveis antes do gateway e prioriza variaveis booleanas, pois elas costumam representar decisoes diretas.

Exemplo:

```text
Caminho "Dados Sim"
Quando [ dadosCompletos ] [ for verdadeiro ]

Caminho "Dados Nao"
Quando [ dadosCompletos ] [ for falso ]
```

## Operadores

Boolean:

- for verdadeiro;
- for falso;
- for igual a.

String:

- e igual a;
- e diferente de;
- esta vazio;
- nao esta vazio.

Number:

- igual;
- maior;
- menor;
- maior ou igual;
- menor ou igual.

## Expressao Tecnica

O ADE gera a expressao tecnica usada pelo Camunda 7. Exemplos:

```text
${dadosCompletos == true}
${status == "CANCELLED"}
${quantidade >= 1}
```

O caminho padrao e explicado como o caminho usado quando nenhuma condicao anterior for satisfeita.
