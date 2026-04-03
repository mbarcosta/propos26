package br.ifes.email_gateway_service.dispatcher;

import br.ifes.email_gateway_service.model.EmailMessage;
import br.ifes.email_gateway_service.model.EmailRule;
import org.springframework.stereotype.Service;

@Service
public class ProcessDispatcher {

    public void dispatch(EmailRule rule, EmailMessage email) {

        if ("start_process".equals(rule.getActionType())) {

            System.out.println(">>> Iniciando processo: " + rule.getProcessKey());
            System.out.println(">>> Email de: " + email.getFrom());

        } else {
            System.out.println(">>> Tipo de ação não suportado: " + rule.getActionType());
        }
    }
}