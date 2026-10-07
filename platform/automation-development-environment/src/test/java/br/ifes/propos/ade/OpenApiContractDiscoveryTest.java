package br.ifes.propos.ade;

import br.ifes.propos.ade.config.DiscoveryProperties;
import br.ifes.propos.ade.discovery.ContractFingerprintService;
import br.ifes.propos.ade.discovery.OpenApiContractDiscovery;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiContractDiscoveryTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final OpenApiContractDiscovery discovery = new OpenApiContractDiscovery(
            mapper, new DiscoveryProperties(), new ContractFingerprintService(mapper));

    @Test
    void discoversUnknownOperationAndNestedArrayWithoutAdeChanges() throws Exception {
        var provider = provider();
        var document = mapper.readTree("""
          {"openapi":"3.0.3","info":{"version":"1"},"paths":{"/novel":{"post":{
            "operationId":"A_CAPABILITY_NEVER_SEEN_BY_ADE","summary":"Novel operation",
            "requestBody":{"required":true,"content":{"application/json":{"schema":{"$ref":"#/components/schemas/Input"}}}},
            "responses":{"200":{"description":"ok","content":{"application/json":{"schema":{"type":"object","properties":{"accepted":{"type":"boolean"}}}}}}}
          }}},"components":{"schemas":{"Input":{"type":"object","required":["items"],"properties":{"items":{"type":"array","items":{"$ref":"#/components/schemas/Member"}}}},"Member":{"type":"object","required":["email"],"properties":{"email":{"type":"string"}}}}}}
          """);
        var capability = discovery.normalize(provider, document).get(0);
        assertThat(capability.id()).isEqualTo("A_CAPABILITY_NEVER_SEEN_BY_ADE");
        assertThat(capability.inputParameters()).singleElement().satisfies(input -> {
            assertThat(input.get("name")).isEqualTo("items");
            assertThat(input.get("type")).isEqualTo("Array<Member>");
            assertThat(input.get("required")).isEqualTo("true");
        });
        assertThat(capability.inputSchema().properties().get(0).items().properties())
                .extracting("name").containsExactly("email");
    }

    @Test
    void fingerprintChangesWhenRequiredContractChanges() throws Exception {
        var first = mapper.readTree(contract("[\"name\"]"));
        var second = mapper.readTree(contract("[\"name\",\"kind\"]"));
        assertThat(discovery.normalize(provider(), first).get(0).contractFingerprint())
                .isNotEqualTo(discovery.normalize(provider(), second).get(0).contractFingerprint());
    }

    private String contract(String required) {
        return "{\"openapi\":\"3.0.3\",\"info\":{\"version\":\"1\"},\"paths\":{\"/x\":{\"post\":{\"operationId\":\"X\",\"requestBody\":{\"content\":{\"application/json\":{\"schema\":{\"type\":\"object\",\"required\":" + required + ",\"properties\":{\"name\":{\"type\":\"string\"},\"kind\":{\"type\":\"string\"}}}}}},\"responses\":{\"204\":{\"description\":\"ok\"}}}}}}";
    }
    private DiscoveryProperties.Provider provider() {
        var provider = new DiscoveryProperties.Provider(); provider.setId("fixture"); provider.setName("Fixture"); return provider;
    }
}
