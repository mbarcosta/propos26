package br.ifes.propos.ade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "ade")
public class DiscoveryProperties {
    private String capabilityCacheFile = "data/discovered-capabilities.json";
    private Discovery discovery = new Discovery();
    private List<Provider> providers = new ArrayList<>();
    public String getCapabilityCacheFile() { return capabilityCacheFile; }
    public void setCapabilityCacheFile(String value) { capabilityCacheFile = value; }
    public Discovery getDiscovery() { return discovery; }
    public void setDiscovery(Discovery value) { discovery = value; }
    public List<Provider> getProviders() { return providers; }
    public void setProviders(List<Provider> value) { providers = value; }

    public static class Discovery {
        private Duration connectTimeout = Duration.ofSeconds(2);
        private Duration readTimeout = Duration.ofSeconds(5);
        private int maxDocumentBytes = 2 * 1024 * 1024;
        private Duration ttl = Duration.ofMinutes(5);
        public Duration getConnectTimeout() { return connectTimeout; }
        public void setConnectTimeout(Duration value) { connectTimeout = value; }
        public Duration getReadTimeout() { return readTimeout; }
        public void setReadTimeout(Duration value) { readTimeout = value; }
        public int getMaxDocumentBytes() { return maxDocumentBytes; }
        public void setMaxDocumentBytes(int value) { maxDocumentBytes = value; }
        public Duration getTtl() { return ttl; }
        public void setTtl(Duration value) { ttl = value; }
    }

    public static class Provider {
        private String id;
        private String name;
        private String contractUrl;
        private String bearerToken;
        public String getId() { return id; }
        public void setId(String value) { id = value; }
        public String getName() { return name; }
        public void setName(String value) { name = value; }
        public String getContractUrl() { return contractUrl; }
        public void setContractUrl(String value) { contractUrl = value; }
        public String getBearerToken() { return bearerToken; }
        public void setBearerToken(String value) { bearerToken = value; }
    }
}
