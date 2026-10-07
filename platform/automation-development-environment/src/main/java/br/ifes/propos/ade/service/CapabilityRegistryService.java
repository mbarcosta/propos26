package br.ifes.propos.ade.service;

import br.ifes.propos.ade.model.AutomationCapability;
import br.ifes.propos.ade.config.DiscoveryProperties;
import br.ifes.propos.ade.discovery.DomainContractDiscovery;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CapabilityRegistryService {

    private final ObjectMapper objectMapper;
    private final DomainContractDiscovery discovery;
    private final DiscoveryProperties properties;
    private List<AutomationCapability> discovered = List.of();
    private Instant lastRefresh;
    private final Map<String, String> providerErrors = new LinkedHashMap<>();

    public CapabilityRegistryService(ObjectMapper objectMapper, DomainContractDiscovery discovery,
                                     DiscoveryProperties properties) {
        this.objectMapper = objectMapper;
        this.discovery = discovery;
        this.properties = properties;
        this.discovered = loadCache();
    }

    public synchronized List<AutomationCapability> capabilities() {
        if (lastRefresh == null || Instant.now().isAfter(lastRefresh.plus(properties.getDiscovery().getTtl()))) {
            refresh();
        }
        Map<String, AutomationCapability> merged = new LinkedHashMap<>();
        staticCapabilities().forEach(item -> merged.put(item.id(), item));
        discovered.forEach(item -> merged.put(item.id(), item));
        return List.copyOf(merged.values());
    }

    public synchronized RefreshResult refresh() {
        List<AutomationCapability> fresh = new ArrayList<>();
        providerErrors.clear();
        for (DiscoveryProperties.Provider provider : properties.getProviders()) {
            try {
                fresh.addAll(discovery.discover(provider));
            } catch (RuntimeException e) {
                providerErrors.put(provider.getId(), e.getMessage());
                discovered.stream().filter(item -> provider.getId().equals(item.provider()))
                        .map(item -> stale(item, e.getMessage())).forEach(fresh::add);
            }
        }
        if (!fresh.isEmpty() || properties.getProviders().isEmpty()) {
            discovered = List.copyOf(fresh);
            saveCache(discovered);
        }
        lastRefresh = Instant.now();
        return new RefreshResult(discovered.size(), lastRefresh, Map.copyOf(providerErrors), providerErrors.isEmpty());
    }

    public synchronized RefreshResult status() {
        return new RefreshResult(discovered.size(), lastRefresh, Map.copyOf(providerErrors), providerErrors.isEmpty());
    }

    public AutomationCapability capability(String id) {
        return capabilities().stream().filter(item -> item.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Capability does not exist: " + id));
    }

    private List<AutomationCapability> staticCapabilities() {
        try (InputStream input = new ClassPathResource("config/capabilities.json").getInputStream()) {
            return objectMapper.readValue(input, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Could not load ADE capability registry", e);
        }
    }

    private List<AutomationCapability> loadCache() {
        Path path = Path.of(properties.getCapabilityCacheFile());
        if (!Files.exists(path)) return List.of();
        try {
            return objectMapper.readValue(path.toFile(), new TypeReference<>() {});
        } catch (IOException e) {
            return List.of();
        }
    }

    private void saveCache(List<AutomationCapability> capabilities) {
        Path path = Path.of(properties.getCapabilityCacheFile());
        try {
            if (path.toAbsolutePath().getParent() != null) Files.createDirectories(path.toAbsolutePath().getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), capabilities);
        } catch (IOException e) {
            providerErrors.put("cache", "Could not persist discovery cache: " + e.getMessage());
        }
    }

    private AutomationCapability stale(AutomationCapability item, String error) {
        return new AutomationCapability(item.id(), item.name(), item.description(), item.type(), item.provider(),
                item.interfaceType(), item.endpoint(), item.inputParameters(), item.outputParameters(),
                item.implementationType(), item.implementation(), item.deployment(), "STALE", item.operationId(),
                item.protocol(), item.method(), item.inputSchema(), item.outputSchema(), item.contractVersion(),
                item.contractFingerprint(), item.lastDiscoveredAt(), true, error);
    }

    public record RefreshResult(int discoveredCapabilities, Instant refreshedAt,
                                Map<String, String> providerErrors, boolean current) {}
}
