package br.ifes.propos.ade.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CirExecutionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String cirBaseUrl;

    public CirExecutionService(@Value("${cir.base-url}") String cirBaseUrl) {
        this.cirBaseUrl = cirBaseUrl;
    }

    public Object execute(String bindingId) {
        try {
            return restTemplate.postForObject(
                    cirBaseUrl + "/api/cir/execute?bindingId=" + bindingId,
                    null,
                    Object.class
            );
        } catch (HttpStatusCodeException e) {
            return Map.of(
                    "status", "FAILED",
                    "component", "CIR",
                    "httpStatus", e.getStatusCode().value(),
                    "message", "CIR execution failed",
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
}
