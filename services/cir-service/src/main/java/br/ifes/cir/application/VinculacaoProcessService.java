package br.ifes.cir.application;

import java.util.Map;

import org.springframework.stereotype.Service;

import br.ifes.cir.client.CamundaClient;
import br.ifes.cir.client.dto.VariableValue;
import br.ifes.cir.domain.model.CirIdentifiedEvent;

@Service
public class VinculacaoProcessService {

    private final CamundaClient camundaClient;

    public VinculacaoProcessService(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    /**
     * Trata o evento de vinculação recebido pelo CIR
     * e dispara o processo correspondente no Camunda.
     */
    public void handleEvent(CirIdentifiedEvent event, String bindingId) {

        Map<String, VariableValue> variables = camundaClient.newVariables();

        variables.put("messageId", camundaClient.stringVar(event.getMessageId()));
        variables.put("from", camundaClient.stringVar(event.getFrom()));
        variables.put("subject", camundaClient.stringVar(event.getSubject()));
        variables.put("bindingId", camundaClient.stringVar(bindingId));
        variables.put("hasAttachments", camundaClient.booleanVar(event.isHasAttachments()));

        camundaClient.sendMessage("VINCULACAO_RECEBIDA", variables);
    }
}
