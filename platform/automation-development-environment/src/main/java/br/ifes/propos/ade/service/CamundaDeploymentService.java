package br.ifes.propos.ade.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.ifes.propos.ade.model.DeploymentRequest;
import br.ifes.propos.ade.model.DeploymentResult;
import br.ifes.propos.ade.model.IntegrationDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CamundaDeploymentService {

    private static final Logger log = LoggerFactory.getLogger(CamundaDeploymentService.class);
    private static final int MAX_DEPLOY_ATTEMPTS = 3;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final RestTemplate restTemplate = restTemplate();
    private final String camundaBaseUrl;
    private final String cirBaseUrl;

    public CamundaDeploymentService(
            @Value("${camunda.base-url}") String camundaBaseUrl,
            @Value("${cir.base-url}") String cirBaseUrl) {
        this.camundaBaseUrl = camundaBaseUrl;
        this.cirBaseUrl = cirBaseUrl;
    }

    public DeploymentResult deploy(DeploymentRequest request) {
        if (request.getBpmnXml() == null || !request.getBpmnXml().contains("<bpmn:definitions")) {
            return DeploymentResult.failed("Invalid BPMN XML");
        }

        String projectKey = valueOrDefault(request.getProjectKey(), "automation");
        String version = valueOrDefault(request.getVersion(), "1.0");
        String deploymentName = projectKey + "-" + version;
        String fileName = projectKey + ".bpmn";

        try {
            log.info("Checking Camunda availability at {}", camundaBaseUrl);
            checkCamundaReachable();
            log.info("Camunda reachable. Creating deployment name={}, file={}", deploymentName, fileName);
            Map<String, Object> response = createDeployment(deploymentName, fileName, request.getBpmnXml());

            String deploymentId = response == null ? null : String.valueOf(response.get("id"));
            log.info("Camunda deployment created: deploymentId={}, name={}", deploymentId, deploymentName);
            String routeMessage = applyCirRoutes(request);
            DeploymentResult result = DeploymentResult.ready(deploymentId, deploymentName);
            if (!routeMessage.isBlank()) {
                result.setMessage(result.getMessage() + ". " + routeMessage);
            }
            return result;
        } catch (HttpStatusCodeException e) {
            log.warn("Camunda deployment rejected with HTTP {}", e.getStatusCode().value());
            return DeploymentResult.failed(camundaHttpErrorMessage(e));
        } catch (ResourceAccessException e) {
            log.warn("Camunda deployment resource access failure: {}", rootMessage(e));
            return DeploymentResult.failed("Camunda deployment failed after " + MAX_DEPLOY_ATTEMPTS
                    + " attempts. Check whether Camunda is reachable at " + camundaBaseUrl
                    + ". Cause: " + rootMessage(e));
        } catch (Exception e) {
            log.warn("Camunda deployment failed: {}", rootMessage(e), e);
            return DeploymentResult.failed(rootMessage(e));
        }
    }

    private void checkCamundaReachable() {
        restTemplate.getForObject(camundaBaseUrl + "/version", String.class);
    }

    private Map<String, Object> createDeployment(String deploymentName, String fileName, String bpmnXml) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_DEPLOY_ATTEMPTS; attempt++) {
            try {
                log.info("Camunda deployment attempt {}/{}", attempt, MAX_DEPLOY_ATTEMPTS);
                return createDeploymentOnce(deploymentName, fileName, bpmnXml);
            } catch (HttpStatusCodeException e) {
                throw e;
            } catch (ResourceAccessException e) {
                lastFailure = e;
                sleepBeforeRetry(attempt);
            } catch (IOException e) {
                lastFailure = new IllegalStateException(e);
                sleepBeforeRetry(attempt);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while deploying BPMN to Camunda", e);
            } catch (RestClientException e) {
                lastFailure = e;
                sleepBeforeRetry(attempt);
            }
        }
        throw lastFailure == null ? new IllegalStateException("Camunda deployment failed") : lastFailure;
    }

    private Map<String, Object> createDeploymentOnce(String deploymentName, String fileName, String bpmnXml)
            throws IOException, InterruptedException {
        String boundary = "----propos26-ade-" + UUID.randomUUID();
        byte[] multipart = multipartBody(boundary, deploymentName, fileName, bpmnXml);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(camundaBaseUrl + "/deployment/create"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipart))
                .build();

        log.info("Posting BPMN to Camunda deployment/create: bytes={}", multipart.length);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        log.info("Camunda deployment/create responded: status={}, responseBytes={}",
                response.statusCode(),
                response.body() == null ? 0 : response.body().getBytes(StandardCharsets.UTF_8).length);
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("Camunda rejected the deployment with HTTP "
                    + response.statusCode() + ". Response: " + response.body());
        }
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    private byte[] multipartBody(String boundary, String deploymentName, String fileName, String bpmnXml) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeFormField(output, boundary, "deployment-name", deploymentName);
        writeFormField(output, boundary, "enable-duplicate-filtering", "true");
        writeFormField(output, boundary, "deploy-changed-only", "true");
        writeFileField(output, boundary, "data", fileName, "text/xml", bpmnXml);
        output.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return output.toByteArray();
    }

    private void writeFormField(ByteArrayOutputStream output, String boundary, String name, String value) throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(value.getBytes(StandardCharsets.UTF_8));
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private void writeFileField(
            ByteArrayOutputStream output,
            String boundary,
            String name,
            String fileName,
            String contentType,
            String content) throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(content.getBytes(StandardCharsets.UTF_8));
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private void sleepBeforeRetry(int attempt) {
        if (attempt >= MAX_DEPLOY_ATTEMPTS) {
            return;
        }
        try {
            Thread.sleep(Duration.ofSeconds(attempt).toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String applyCirRoutes(DeploymentRequest request) {
        java.util.ArrayList<Map<String, Object>> routes = new java.util.ArrayList<>();
        IntegrationDefinition integration = request.getIntegration();
        if (integration != null && integration.getExternalEvent() != null && !integration.getExternalEvent().isBlank()) {
            String externalEvent = integration.getExternalEvent();
            String messageName = valueOrDefault(integration.getCamundaMessage(), externalEvent);
            String correlationField = valueOrDefault(integration.getCorrelationField(), "correlationId");
            String processDefinitionKey = extractProcessId(request.getBpmnXml());

            routes.add(Map.of(
                "externalEvent", externalEvent,
                "action", "START_PROCESS",
                "messageName", messageName,
                "processDefinitionKey", processDefinitionKey,
                "businessKeyVariable", correlationField,
                "correlationVariable", correlationField,
                "subjectContains", valueOrDefault(integration.getSubjectContains(), externalEvent.toLowerCase().replace("_", " "))
            ));
        }

        if (request.getInboundIntegrations() != null) {
            for (IntegrationDefinition inbound : request.getInboundIntegrations()) {
                if (inbound.getExternalEvent() == null || inbound.getExternalEvent().isBlank()) {
                    continue;
                }
                String externalEvent = inbound.getExternalEvent();
                String messageName = valueOrDefault(inbound.getCamundaMessage(), externalEvent);
                String correlationField = valueOrDefault(inbound.getCorrelationField(), "correlationId");
                routes.add(Map.of(
                        "externalEvent", externalEvent,
                        "action", "CORRELATE_MESSAGE",
                        "messageName", messageName,
                        "correlationVariable", correlationField,
                        "correlationExpression", valueOrDefault(inbound.getCorrelationExpression(), "${" + correlationField + "}"),
                        "variableMappings", valueOrDefault(inbound.getVariableMappings(), "{}")
                ));
            }
        }

        if (!routes.isEmpty()) {
            try {
                log.info("Publishing {} CIR route(s) to {}", routes.size(), cirBaseUrl);
                restTemplate.postForObject(cirBaseUrl + "/api/cir/routes", routes, Object.class);
                log.info("CIR routes published");
                return "Published " + routes.size() + " CIR route(s).";
            } catch (Exception e) {
                log.warn("CIR route publishing failed: {}", rootMessage(e));
                return "BPMN was deployed, but CIR route publishing failed: " + rootMessage(e);
            }
        }
        return "No CIR routes to publish.";
    }

    private String extractProcessId(String bpmnXml) {
        Matcher matcher = Pattern.compile("<bpmn:process\\s+id=\"([^\"]+)\"").matcher(bpmnXml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "automation_process";
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30));
        return new RestTemplate(factory);
    }

    private String camundaHttpErrorMessage(HttpStatusCodeException e) {
        int status = e.getStatusCode().value();
        if (status == 401 || status == 403) {
            return "Camunda rejected the deployment with HTTP " + status
                    + ". Check REST authentication for " + camundaBaseUrl + ". Response: " + e.getResponseBodyAsString();
        }
        return "Camunda rejected the deployment with HTTP " + status + ". Response: " + e.getResponseBodyAsString();
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? throwable.getMessage() : current.getMessage();
    }
}
