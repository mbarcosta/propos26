package br.ifes.propos.ade.discovery;

import br.ifes.propos.ade.config.DiscoveryProperties;
import br.ifes.propos.ade.model.AutomationCapability;
import br.ifes.propos.ade.model.CapabilitySchema;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class OpenApiContractDiscovery implements DomainContractDiscovery {
    private static final Set<String> METHODS = Set.of("get", "post", "put", "patch", "delete", "head", "options");
    private final ObjectMapper jsonMapper;
    private final DiscoveryProperties properties;
    private final ContractFingerprintService fingerprints;

    public OpenApiContractDiscovery(ObjectMapper jsonMapper, DiscoveryProperties properties,
                                    ContractFingerprintService fingerprints) {
        this.jsonMapper = jsonMapper;
        this.properties = properties;
        this.fingerprints = fingerprints;
    }

    @Override
    public List<AutomationCapability> discover(DiscoveryProperties.Provider provider) {
        URI uri = registeredUri(provider);
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(properties.getDiscovery().getConnectTimeout())
                    .followRedirects(HttpClient.Redirect.NEVER).build();
            HttpRequest.Builder request = HttpRequest.newBuilder(uri)
                    .timeout(properties.getDiscovery().getReadTimeout())
                    .header("Accept", "application/json, application/yaml, text/yaml").GET();
            if (provider.getBearerToken() != null && !provider.getBearerToken().isBlank()) {
                request.header("Authorization", "Bearer " + provider.getBearerToken());
            }
            HttpResponse<byte[]> response = client.send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("provider returned HTTP " + response.statusCode());
            }
            if (response.body().length > properties.getDiscovery().getMaxDocumentBytes()) {
                throw new IllegalStateException("OpenAPI document exceeds configured size limit");
            }
            if (!looksLikeJson(response.body())) throw new IllegalStateException("Registered provider must publish OpenAPI as JSON");
            JsonNode document = jsonMapper.readTree(new ByteArrayInputStream(response.body()));
            validateDocument(document);
            return normalize(provider, document);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OpenAPI discovery interrupted for " + provider.getId(), e);
        } catch (Exception e) {
            throw new IllegalStateException("Could not update contract for registered provider " + provider.getId()
                    + ": " + e.getMessage(), e);
        }
    }

    private URI registeredUri(DiscoveryProperties.Provider provider) {
        if (provider.getId() == null || provider.getId().isBlank() || provider.getContractUrl() == null) {
            throw new IllegalStateException("Registered provider has incomplete metadata");
        }
        URI uri = URI.create(provider.getContractUrl());
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null) {
            throw new IllegalStateException("Provider contract URL must be an absolute HTTP(S) URL");
        }
        return uri;
    }

    private boolean looksLikeJson(byte[] body) {
        for (byte value : body) {
            if (!Character.isWhitespace(value)) return value == '{' || value == '[';
        }
        return true;
    }

    private void validateDocument(JsonNode document) {
        if (document == null || !document.path("openapi").asText().startsWith("3.")) {
            throw new IllegalStateException("Only OpenAPI 3.x documents are supported");
        }
        if (!document.path("paths").isObject()) {
            throw new IllegalStateException("OpenAPI document has no paths object");
        }
    }

    public List<AutomationCapability> normalize(DiscoveryProperties.Provider provider, JsonNode document) {
        List<AutomationCapability> result = new ArrayList<>();
        String version = document.path("info").path("version").asText("");
        Instant discoveredAt = Instant.now();
        document.path("paths").fields().forEachRemaining(pathEntry ->
                pathEntry.getValue().fields().forEachRemaining(methodEntry -> {
                    String method = methodEntry.getKey().toLowerCase(Locale.ROOT);
                    if (!METHODS.contains(method)) return;
                    JsonNode operation = methodEntry.getValue();
                    String operationId = operation.path("operationId").asText();
                    if (operationId.isBlank()) operationId = generatedOperationId(method, pathEntry.getKey());
                    CapabilitySchema input = inputSchema(document, pathEntry.getValue(), operation);
                    CapabilitySchema output = outputSchema(document, operation);
                    List<Map<String, String>> inputs = parameters(input);
                    List<Map<String, String>> outputs = parameters(output);
                    JsonNode normalized = jsonMapper.valueToTree(Map.of(
                            "method", method.toUpperCase(Locale.ROOT), "endpoint", pathEntry.getKey(),
                            "input", input, "output", output));
                    String name = operation.path("summary").asText(operationId);
                    String description = operation.path("description").asText(name);
                    result.add(new AutomationCapability(
                            operationId, name, description, "DOMAIN_OPERATION", provider.getId(), "REST",
                            pathEntry.getKey(), inputs, outputs, "REST", method.toUpperCase(Locale.ROOT)
                            + " " + pathEntry.getKey(), provider.getName(), "AVAILABLE", operationId, "HTTP",
                            method.toUpperCase(Locale.ROOT), input, output, version, fingerprints.fingerprint(normalized),
                            discoveredAt, false, null));
                }));
        return result;
    }

    private CapabilitySchema inputSchema(JsonNode document, JsonNode pathItem, JsonNode operation) {
        List<CapabilitySchema> properties = new ArrayList<>();
        collectParameters(document, pathItem.path("parameters"), properties);
        collectParameters(document, operation.path("parameters"), properties);
        JsonNode requestBody = resolveNode(document, operation.path("requestBody"));
        JsonNode bodySchema = firstContentSchema(requestBody.path("content"));
        if (!bodySchema.isMissingNode()) {
            CapabilitySchema body = schema(document, "body", bodySchema, requestBody.path("required").asBoolean(), new LinkedHashSet<>());
            if (!body.properties().isEmpty()) properties.addAll(body.properties());
            else properties.add(body);
        }
        return new CapabilitySchema("input", "Object", null, true, null, List.of(), properties, null, null);
    }

    private void collectParameters(JsonNode document, JsonNode array, List<CapabilitySchema> target) {
        if (!array.isArray()) return;
        for (JsonNode raw : array) {
            JsonNode parameter = resolveNode(document, raw);
            target.add(schema(document, parameter.path("name").asText("parameter"), parameter.path("schema"),
                    parameter.path("required").asBoolean(), new LinkedHashSet<>()));
        }
    }

    private CapabilitySchema outputSchema(JsonNode document, JsonNode operation) {
        JsonNode responses = operation.path("responses");
        Iterator<Map.Entry<String, JsonNode>> fields = responses.fields();
        JsonNode response = null;
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            if (entry.getKey().startsWith("2")) { response = resolveNode(document, entry.getValue()); break; }
        }
        JsonNode responseSchema = response == null ? jsonMapper.missingNode() : firstContentSchema(response.path("content"));
        if (responseSchema.isMissingNode()) {
            return new CapabilitySchema("output", "Object", null, false, null, List.of(), List.of(), null, null);
        }
        CapabilitySchema output = schema(document, "output", responseSchema, false, new LinkedHashSet<>());
        return !output.properties().isEmpty() ? output
                : new CapabilitySchema("output", "Object", null, false, null, List.of(), List.of(output), null, null);
    }

    private JsonNode firstContentSchema(JsonNode content) {
        if (!content.isObject()) return jsonMapper.missingNode();
        Iterator<JsonNode> values = content.elements();
        return values.hasNext() ? values.next().path("schema") : jsonMapper.missingNode();
    }

    private CapabilitySchema schema(JsonNode document, String name, JsonNode raw, boolean required, Set<String> resolving) {
        String reference = raw.path("$ref").asText(null);
        if (reference != null) {
            if (!resolving.add(reference)) return new CapabilitySchema(name, referenceName(reference), null, required, null, List.of(), List.of(), null, reference);
            CapabilitySchema resolved = schema(document, name, resolveReference(document, reference), required, resolving);
            resolving.remove(reference);
            String resolvedType = resolved.properties().isEmpty() ? resolved.type() : referenceName(reference);
            return new CapabilitySchema(resolved.name(), resolvedType, resolved.format(), required,
                    resolved.description(), resolved.enumValues(), resolved.properties(), resolved.items(), reference);
        }
        String primitive = raw.path("type").asText(raw.has("properties") ? "object" : "object");
        String type = normalizedType(primitive, raw.path("format").asText(null), raw);
        List<String> enums = new ArrayList<>();
        raw.path("enum").forEach(value -> enums.add(value.asText()));
        if ("Array".equals(type)) {
            CapabilitySchema items = schema(document, "item", raw.path("items"), true, resolving);
            return new CapabilitySchema(name, "Array<" + items.type() + ">", raw.path("format").asText(null), required,
                    raw.path("description").asText(null), enums, List.of(), items, reference);
        }
        List<CapabilitySchema> properties = new ArrayList<>();
        Set<String> requiredNames = new LinkedHashSet<>();
        raw.path("required").forEach(item -> requiredNames.add(item.asText()));
        raw.path("properties").fields().forEachRemaining(entry -> properties.add(schema(document, entry.getKey(),
                entry.getValue(), requiredNames.contains(entry.getKey()), resolving)));
        if (raw.path("allOf").isArray()) {
            for (JsonNode component : raw.path("allOf")) {
                CapabilitySchema part = schema(document, name, component, required, resolving);
                for (CapabilitySchema property : part.properties()) {
                    if (properties.stream().noneMatch(existing -> existing.name().equals(property.name()))) properties.add(property);
                }
            }
        }
        return new CapabilitySchema(name, type, raw.path("format").asText(null), required,
                raw.path("description").asText(null), enums, properties, null, reference);
    }

    private JsonNode resolveNode(JsonNode document, JsonNode node) {
        return node.has("$ref") ? resolveReference(document, node.path("$ref").asText()) : node;
    }

    private JsonNode resolveReference(JsonNode document, String reference) {
        if (!reference.startsWith("#/")) throw new IllegalStateException("External OpenAPI references are not allowed: " + reference);
        JsonNode resolved = document.at(reference.substring(1));
        if (resolved.isMissingNode()) throw new IllegalStateException("Unresolved OpenAPI reference: " + reference);
        return resolved;
    }

    private String normalizedType(String type, String format, JsonNode schema) {
        if (schema.has("enum")) return "Enum";
        return switch (type) {
            case "string" -> "date".equals(format) ? "Date" : "date-time".equals(format) ? "DateTime" : "String";
            case "boolean" -> "Boolean";
            case "integer" -> "int64".equals(format) ? "Long" : "Integer";
            case "number" -> "Decimal";
            case "array" -> "Array";
            default -> "Object";
        };
    }

    private List<Map<String, String>> parameters(CapabilitySchema root) {
        List<Map<String, String>> result = new ArrayList<>();
        for (CapabilitySchema property : root.properties()) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("name", property.name());
            item.put("type", property.type());
            item.put("required", Boolean.toString(property.required()));
            if (property.format() != null) item.put("format", property.format());
            result.add(item);
        }
        return result;
    }

    private String generatedOperationId(String method, String path) {
        return (method + "_" + path).replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_|_$", "").toUpperCase(Locale.ROOT);
    }
    private String referenceName(String reference) { return reference.substring(reference.lastIndexOf('/') + 1); }
}
