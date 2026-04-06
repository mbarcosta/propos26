package br.ifes.cir.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import br.ifes.cir.client.dto.StartProcessRequest;
import br.ifes.cir.client.dto.VariableValue;
import br.ifes.cir.domain.model.CirIdentifiedEvent;

/**
 * Cliente responsável pela integração REAL com o Camunda 7 via REST.
 *
 * <p>Este componente encapsula todas as chamadas ao engine,
 * funcionando como uma façade.</p>
 */
@Component
public class CamundaClient {

    private final RestTemplate restTemplate;

    /**
     * URL base do Camunda REST API.
     * Exemplo: http://localhost:8080/engine-rest
     */
    @Value("${camunda.base-url}")
    private String baseUrl;

    public CamundaClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Inicia uma nova instância de processo no Camunda.
     *
     * @param processDefinitionKey chave do processo
     * @param event evento de origem
     */
    public void startProcess(String processDefinitionKey, CirIdentifiedEvent event) {

        String url = baseUrl + "/process-definition/key/" + processDefinitionKey + "/start";

        /*
         * Montagem das variáveis no formato esperado pelo Camunda.
         */
        Map<String, VariableValue> variables = new HashMap<>();

        variables.put("messageId", new VariableValue(event.getMessageId(), "String"));
        variables.put("from", new VariableValue(event.getFrom(), "String"));
        variables.put("subject", new VariableValue(event.getSubject(), "String"));
        variables.put("hasAttachments", new VariableValue(event.isHasAttachments(), "Boolean"));

        StartProcessRequest request = new StartProcessRequest();
        request.setVariables(variables);

        /*
         * Chamada REST ao Camunda.
         */
        restTemplate.postForObject(url, request, String.class);
    }

    /**
     * Correlaciona uma mensagem com uma instância existente.
     *
     * (implementação opcional no próximo passo)
     */
    public void correlateMessage(String messageName, CirIdentifiedEvent event) {
        throw new UnsupportedOperationException("Ainda não implementado");
    }
}