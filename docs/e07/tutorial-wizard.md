# E07 - Tutorial Wizard

O Automation Configuration Wizard do ADE foi evoluido para atuar como assistente tutorial em portugues. A estrutura modal criada no E06 foi preservada: header, progresso lateral, area central rolavel e footer fixo com Voltar, Cancelar, Avancar e Concluir.

O fluxo de cada etapa segue a ordem:

1. explicar o papel do elemento BPMN;
2. perguntar a intencao de configuracao;
3. sugerir opcoes deterministicamente;
4. permitir escolha do usuario;
5. validar a configuracao;
6. resumir a consequencia em execucao.

Os termos tecnicos continuam disponiveis em `Ver detalhes tecnicos`, incluindo BPMN ID, BPMN Type, requirements, capability, provider, endpoint, router, message name e implementation type.

## Etapas Guiadas

Start Message Event e Message Catch Event explicam como GMS, CIR e Camunda participam da entrada ou correlacao da mensagem. O usuario configura canal, evento externo, nome da mensagem, identificador de correlacao e dados extraidos.

Service Task explica que a tarefa sera executada por uma capability. O Wizard mostra funcionalidades compativeis antes do catalogo completo e exibe o que a capability precisa e devolve.

Send Task orienta envio de e-mail com destinatario, assunto, mensagem e resposta esperada. Campos que aceitam variavel ou literal usam fonte de valor explicita.

Exclusive Gateway explica a decisao, lista dados disponiveis naquele ponto e oferece builder visual de condicoes.

## Diagnostico

Mensagens tecnicas como `INFO` e `ERROR` foram substituidas na linguagem principal por mensagens acionaveis:

- `Esta etapa esta configurada corretamente.`
- `Escolha qual funcionalidade executara esta tarefa.`
- `O dado obrigatorio studentId ainda nao foi associado a nenhum dado do processo.`

O diagnostico tecnico permanece disponivel nos detalhes.
