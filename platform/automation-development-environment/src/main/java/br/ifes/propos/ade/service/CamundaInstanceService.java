package br.ifes.propos.ade.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CamundaInstanceService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String camundaBaseUrl;

    public CamundaInstanceService(@Value("${camunda.base-url}") String camundaBaseUrl) {
        this.camundaBaseUrl = camundaBaseUrl;
    }

    public Object instances(String processDefinitionKey) {
        try {
            String url = camundaBaseUrl + "/process-instance?processDefinitionKey=" + processDefinitionKey;
            return restTemplate.getForObject(url, Object.class);
        } catch (HttpStatusCodeException e) {
            return Map.of(
                    "status", "FAILED",
                    "component", "Camunda",
                    "httpStatus", e.getStatusCode().value(),
                    "message", "Could not list process instances",
                    "details", e.getResponseBodyAsString()
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "FAILED",
                    "component", "ADE",
                    "message", e.getMessage()
            );
        }
    }

    public Map<String, Object> cancelInstance(String instanceId) {
        try {
            restTemplate.delete(camundaBaseUrl + "/process-instance/" + instanceId);
            return Map.of("status", "CANCELLED", "instanceId", instanceId);
        } catch (HttpStatusCodeException e) {
            return Map.of(
                    "status", "FAILED",
                    "component", "Camunda",
                    "httpStatus", e.getStatusCode().value(),
                    "instanceId", instanceId,
                    "details", e.getResponseBodyAsString()
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "FAILED",
                    "component", "ADE",
                    "instanceId", instanceId,
                    "message", e.getMessage()
            );
        }
    }
}
