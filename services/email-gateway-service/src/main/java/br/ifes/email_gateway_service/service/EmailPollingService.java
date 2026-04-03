package br.ifes.email_gateway_service.service;

import br.ifes.email_gateway_service.dispatcher.ProcessDispatcher;
import br.ifes.email_gateway_service.model.EmailEvent;
import br.ifes.email_gateway_service.model.EmailMessage;
import br.ifes.email_gateway_service.model.EmailRule;
import br.ifes.email_gateway_service.model.PollItemResult;
import br.ifes.email_gateway_service.model.PollResult;
import br.ifes.email_gateway_service.repository.MailBindingRepository;
import br.ifes.email_gateway_service.model.MailBinding;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço responsável por executar o polling de uma caixa postal
 * configurada por um binding, aplicar as regras correspondentes
 * e produzir eventos consumíveis por clientes externos.
 *
 * Política atual de processamento:
 * - mensagens que não casam com nenhuma regra do binding:
 *   permanecem na INBOX e são registradas como IGNORED
 *
 * - mensagens que casam e são processadas com sucesso:
 *   geram evento e são movidas para a pasta Processed
 *
 * - mensagens que casam, mas falham no processamento:
 *   permanecem na INBOX e são registradas como ERROR
 *
 * O objetivo desta classe é atuar como núcleo do gateway genérico
 * de eventos de e-mail.
 */
@Service
public class EmailPollingService {

    private final MailBindingRepository bindingRepository;
    private final MailReaderService mailReaderService;
    private final RuleMatcher ruleMatcher;
    private final ProcessDispatcher processDispatcher;
    private final MailPostProcessorService postProcessor;

    public EmailPollingService(MailBindingRepository bindingRepository,
                               MailReaderService mailReaderService,
                               RuleMatcher ruleMatcher,
                               ProcessDispatcher processDispatcher,
                               MailPostProcessorService postProcessor) {
        this.bindingRepository = bindingRepository;
        this.mailReaderService = mailReaderService;
        this.ruleMatcher = ruleMatcher;
        this.processDispatcher = processDispatcher;
        this.postProcessor = postProcessor;
    }

    /**
     * Executa o polling do binding informado.
     *
     * @param bindingId identificador do binding
     * @return resultado consolidado da execução, contendo resumo operacional
     *         e eventos produzidos
     */
    public PollResult poll(String bindingId) {

        PollResult pollResult = new PollResult();
        pollResult.setBindingId(bindingId);

        var binding = bindingRepository.findById(bindingId);

        if (binding == null) {
            throw new RuntimeException("Binding não encontrado: " + bindingId);
        }

        if (!binding.isActive()) {
            throw new RuntimeException("Binding inativo: " + bindingId);
        }

        List<EmailMessage> emails = mailReaderService.readEmails(binding);

        for (EmailMessage email : emails) {
            PollItemResult itemResult = new PollItemResult();
            itemResult.setFrom(email.getFrom());
            itemResult.setSubject(email.getSubject());

            try {
                EmailRule rule = ruleMatcher.match(email, binding.getRules());

                if (rule == null) {
                    processIgnoredEmail(pollResult, itemResult);
                } else {
                    processMatchedEmail(binding.getId(), binding, email, rule, pollResult, itemResult);
                }

            } catch (Exception e) {
                processErrorEmail(pollResult, itemResult, e);
            }

            pollResult.getResults().add(itemResult);
        }

        return pollResult;
    }

    /**
     * Trata uma mensagem que não casou com nenhuma regra do binding.
     *
     * Política:
     * - não altera a mensagem
     * - não move
     * - não marca como lida
     * - apenas registra como IGNORED
     */
    private void processIgnoredEmail(PollResult pollResult, PollItemResult itemResult) {
        itemResult.setStatus("IGNORED");
        itemResult.setMessage("Nenhuma regra encontrada para o e-mail");
        pollResult.setIgnoredCount(pollResult.getIgnoredCount() + 1);
    }

    /**
     * Trata uma mensagem que casou com uma regra do binding.
     *
     * Política:
     * - despacha a ação
     * - move a mensagem para Processed
     * - gera um evento consumível
     * - registra como PROCESSED
     */
    private void processMatchedEmail(String bindingId,
            MailBinding binding,
            EmailMessage email,
            EmailRule rule,
            PollResult pollResult,
            PollItemResult itemResult) {
        processDispatcher.dispatch(rule, email);
        postProcessor.moveToProcessed(binding, email);

        EmailEvent event = buildEvent(bindingId, rule, email);
        pollResult.getEvents().add(event);

        itemResult.setRuleName(rule.getName());
        itemResult.setStatus("PROCESSED");
        itemResult.setMessage("E-mail processado com sucesso");
        pollResult.setProcessedCount(pollResult.getProcessedCount() + 1);
    }

    /**
     * Trata erro ocorrido durante o processamento de uma mensagem.
     *
     * Política:
     * - não altera a mensagem
     * - não move
     * - apenas registra erro
     */
    private void processErrorEmail(PollResult pollResult,
                                   PollItemResult itemResult,
                                   Exception e) {
        itemResult.setStatus("ERROR");
        itemResult.setMessage("Erro ao processar e-mail: " + e.getMessage());
        pollResult.setErrorCount(pollResult.getErrorCount() + 1);
    }

    /**
     * Constrói um evento de e-mail a partir da regra casada e da mensagem.
     *
     * @param bindingId identificador do binding
     * @param rule regra que casou com o e-mail
     * @param email mensagem processada
     * @return evento estruturado pronto para consumo por clientes externos
     */
    private EmailEvent buildEvent(String bindingId, EmailRule rule, EmailMessage email) {
        EmailEvent event = new EmailEvent();
        event.setBindingId(bindingId);
        event.setRuleName(rule.getName());
        event.setActionType(rule.getActionType());
        event.setProcessKey(rule.getProcessKey());
        event.setFrom(email.getFrom());
        event.setSubject(email.getSubject());
        event.setBody(email.getBody());
        event.setMessageNumber(email.getMessageNumber());
        return event;
    }
}