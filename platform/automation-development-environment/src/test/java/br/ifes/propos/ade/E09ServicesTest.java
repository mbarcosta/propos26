package br.ifes.propos.ade;

import br.ifes.propos.ade.config.DiscoveryProperties;
import br.ifes.propos.ade.model.AutomationCapability;
import br.ifes.propos.ade.model.DataRequirement;
import br.ifes.propos.ade.model.DataResolutionPlan;
import br.ifes.propos.ade.service.CapabilityRegistryService;
import br.ifes.propos.ade.service.DataRequirementResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class E09ServicesTest {
    @TempDir Path temp;

    @Test
    void stringBodyCannotMapToLongAndReactiveExtractionRemovesGap() {
        AutomationCapability capability = capability("DYNAMIC", List.of(Map.of("name", "studentId", "type", "Long", "required", "true")));
        DataRequirementResolver resolver = new DataRequirementResolver();
        var available = List.of(new DataRequirementResolver.ProcessData("body", "String", "email"));
        var invalidMapping = new DataResolutionPlan(List.of(new DataResolutionPlan.Entry("studentId",
                DataResolutionPlan.Strategy.MAPPING, "body", null, null, null)));
        assertThat(resolver.resolve(capability, available, invalidMapping).get(0).status()).isEqualTo(DataRequirement.Status.TYPE_MISMATCH);

        var plan = new DataResolutionPlan(List.of(new DataResolutionPlan.Entry("studentId",
                DataResolutionPlan.Strategy.MESSAGE_EXTRACTION, "body", null, null, null)));
        assertThat(resolver.resolve(capability, available, plan).get(0).status()).isEqualTo(DataRequirement.Status.SATISFIED);
    }

    @Test
    void unavailableProviderReturnsStaleLastKnownContract() {
        DiscoveryProperties properties = new DiscoveryProperties();
        properties.setCapabilityCacheFile(temp.resolve("cache.json").toString());
        DiscoveryProperties.Provider provider = new DiscoveryProperties.Provider();
        provider.setId("provider"); provider.setName("Provider"); provider.setContractUrl("http://registered.invalid/v3/api-docs");
        properties.setProviders(List.of(provider));
        AtomicBoolean available = new AtomicBoolean(true);
        var service = new CapabilityRegistryService(new ObjectMapper().findAndRegisterModules(), ignored -> {
            if (!available.get()) throw new IllegalStateException("offline");
            return List.of(capability("RUNTIME_ONLY", List.of()));
        }, properties);
        assertThat(service.refresh().current()).isTrue();
        available.set(false);
        assertThat(service.refresh().current()).isFalse();
        assertThat(service.capability("RUNTIME_ONLY").stale()).isTrue();
        assertThat(service.capability("RUNTIME_ONLY").discoveryError()).contains("offline");
    }

    private AutomationCapability capability(String id, List<Map<String, String>> inputs) {
        return new AutomationCapability(id, id, id, "DOMAIN_OPERATION", "provider", "REST", "/dynamic",
                inputs, List.of(), "REST", "POST /dynamic", "provider", "AVAILABLE", id, "HTTP", "POST",
                null, null, "1", "fingerprint", Instant.now(), false, null);
    }
}
