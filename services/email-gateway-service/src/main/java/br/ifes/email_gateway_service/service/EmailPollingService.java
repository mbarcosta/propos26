package br.ifes.email_gateway_service.service;

import br.ifes.email_gateway_service.model.EmailMessage;
import br.ifes.email_gateway_service.model.MailBinding;
import br.ifes.email_gateway_service.model.PollItemResult;
import br.ifes.email_gateway_service.model.PollResult;
import br.ifes.email_gateway_service.repository.MailBindingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailPollingService {

    private final MailBindingRepository bindingRepository;
    private final MailReaderService mailReaderService;
    private final MailPostProcessorService mailPostProcessorService;

    public EmailPollingService(MailBindingRepository bindingRepository,
                               MailReaderService mailReaderService,
                               MailPostProcessorService mailPostProcessorService) {
        this.bindingRepository = bindingRepository;
        this.mailReaderService = mailReaderService;
        this.mailPostProcessorService = mailPostProcessorService;
    }

    /**
     * Executa o polling do binding informado e devolve as mensagens lidas.
     *
     * @param bindingId identificador do binding
     * @return resultado consolidado do polling
     */
    public PollResult poll(String bindingId) {
        PollResult pollResult = new PollResult();
        pollResult.setBindingId(bindingId);

        MailBinding binding = bindingRepository.findById(bindingId);

        if (binding == null) {
            throw new RuntimeException("Binding não encontrado: " + bindingId);
        }

        if (!binding.isActive()) {
            throw new RuntimeException("Binding inativo: " + bindingId);
        }

        List<EmailMessage> emails = mailReaderService.readEmails(binding);

        pollResult.setMessages(emails);
        pollResult.setTotalRead(emails.size());

        for (EmailMessage email : emails) {
            PollItemResult itemResult = new PollItemResult();
            itemResult.setFrom(email.getFrom());
            itemResult.setSubject(email.getSubject());
            itemResult.setStatus("READ");
            itemResult.setMessage("E-mail lido com sucesso");

            pollResult.getResults().add(itemResult);
        }

        return pollResult;
    }

    /**
     * Marca uma mensagem como processada, movendo-a para a pasta Processed.
     *
     * @param bindingId identificador do binding
     * @param messageNumber número da mensagem na pasta
     */
    public void markAsProcessed(String bindingId, int messageNumber) {
        MailBinding binding = bindingRepository.findById(bindingId);

        if (binding == null) {
            throw new RuntimeException("Binding não encontrado: " + bindingId);
        }

        if (!binding.isActive()) {
            throw new RuntimeException("Binding inativo: " + bindingId);
        }

        EmailMessage email = new EmailMessage();
        email.setMessageNumber(messageNumber);

        mailPostProcessorService.moveToProcessed(binding, email);
    }
}