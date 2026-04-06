package br.ifes.cir.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import br.ifes.cir.client.dto.GmsPollResult;
import br.ifes.cir.client.dto.MarkAsProcessedRequest;

/**
 * Cliente responsável pela comunicação do CIR com o GMS.
 */
@Component
public class GmsClient {

    private final RestTemplate restTemplate;

    public GmsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Executa o polling de um binding no GMS.
     *
     * @param bindingId identificador do binding
     * @return resultado retornado pelo GMS
     */
    public GmsPollResult poll(String bindingId) {
        String url = "http://localhost:8081/api/bindings/" + bindingId + "/poll";
        return restTemplate.postForObject(url, null, GmsPollResult.class);
    }

    /**
     * Solicita ao GMS que marque uma mensagem como processada.
     *
     * <p>O contrato entre CIR e GMS usa {@code messageId} como identificador
     * de integração, enviado no corpo da requisição.</p>
     *
     * @param bindingId identificador do binding
     * @param messageId identificador técnico da mensagem
     */
    public void moveToProcessed(String bindingId, String messageId) {
        String url = "http://localhost:8081/api/bindings/" + bindingId + "/messages/processed";
        MarkAsProcessedRequest request = new MarkAsProcessedRequest(messageId);
        restTemplate.postForObject(url, request, Void.class);
    }
}