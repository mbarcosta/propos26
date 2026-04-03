# Email Gateway Service

## Objetivo
Serviço para leitura de e-mails via IMAP, aplicação de regras e geração de eventos para sistemas externos (Camunda, n8n, etc.).

## Arquitetura
Entrada (Email) → Regras → Evento → Cliente

## Componentes
- EmailPollingController
- EmailPollingService
- MailReaderService
- RuleMatcher
- MailPostProcessorService

## Binding
Define caixa, credenciais e regras.

## Fluxo
1. Busca emails UNSEEN
2. Aplica regras
3. Se match:
   - gera evento
   - move para Processed
4. Se não match:
   - mantém INBOX
5. Se erro:
   - mantém INBOX

## Configuração importante
mail.imap.peek=true
mail.imaps.peek=true

## Endpoint
POST /api/bindings/{bindingId}/poll

## Resultado
Retorna eventos processados.

## Evolução futura
- anexos
- integração com Camunda
- arquitetura orientada a eventos
