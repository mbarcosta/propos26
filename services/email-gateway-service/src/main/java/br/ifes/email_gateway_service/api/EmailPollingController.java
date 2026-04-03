package br.ifes.email_gateway_service.api;

import br.ifes.email_gateway_service.model.PollResult;
import br.ifes.email_gateway_service.service.EmailPollingService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsável por expor a operação de polling
 * de eventos de e-mail para um binding específico.
 */
@RestController
@RequestMapping("/api/bindings")
public class EmailPollingController {

    private final EmailPollingService pollingService;

    public EmailPollingController(EmailPollingService pollingService) {
        this.pollingService = pollingService;
    }

    /**
     * Executa o polling do binding informado.
     *
     * @param bindingId identificador do binding
     * @return resultado consolidado da operação,
     *         incluindo resumo operacional e eventos produzidos
     */
    @PostMapping("/{bindingId}/poll")
    public PollResult poll(@PathVariable String bindingId) {
        return pollingService.poll(bindingId);
    }
}