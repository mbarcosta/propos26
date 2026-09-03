package br.ifes.propos.ade.service;

import br.ifes.propos.ade.model.AutomationCapability;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class CapabilityRegistryService {

    private final ObjectMapper objectMapper;

    public CapabilityRegistryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<AutomationCapability> capabilities() {
        try (InputStream input = new ClassPathResource("config/capabilities.json").getInputStream()) {
            return objectMapper.readValue(input, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Could not load ADE capability registry", e);
        }
    }
}
