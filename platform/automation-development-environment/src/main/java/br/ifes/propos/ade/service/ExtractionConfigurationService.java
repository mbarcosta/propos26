package br.ifes.propos.ade.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class ExtractionConfigurationService {
    private final RestTemplate client = new RestTemplate();
    private final String cirBaseUrl;
    public ExtractionConfigurationService(@Value("${cir.base-url}") String cirBaseUrl) { this.cirBaseUrl = cirBaseUrl; }
    public Object publish(String event, Map<String, Object> contract) {
        String url = UriComponentsBuilder.fromUriString(cirBaseUrl).pathSegment("api", "cir", "extractions", event)
                .build().encode().toUriString();
        client.put(url, contract);
        return client.getForObject(url.substring(0, url.lastIndexOf('/')), Object.class);
    }
}
