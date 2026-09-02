package br.ifes.propos.ade.api;

import br.ifes.propos.ade.model.AutomationCapability;
import br.ifes.propos.ade.model.DeploymentRequest;
import br.ifes.propos.ade.model.DeploymentResult;
import br.ifes.propos.ade.service.CamundaDeploymentService;
import br.ifes.propos.ade.service.CirExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdeController {

    private static final Logger log = LoggerFactory.getLogger(AdeController.class);

    private final CamundaDeploymentService deploymentService;
    private final CirExecutionService cirExecutionService;
    private final String camundaBaseUrl;
    private final String cirBaseUrl;
    private final String gmsBaseUrl;

    public AdeController(
            CamundaDeploymentService deploymentService,
            CirExecutionService cirExecutionService,
            @Value("${camunda.base-url}") String camundaBaseUrl,
            @Value("${cir.base-url}") String cirBaseUrl,
            @Value("${gms.base-url}") String gmsBaseUrl) {
        this.deploymentService = deploymentService;
        this.cirExecutionService = cirExecutionService;
        this.camundaBaseUrl = camundaBaseUrl;
        this.cirBaseUrl = cirBaseUrl;
        this.gmsBaseUrl = gmsBaseUrl;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "automation-development-environment");
    }

    @GetMapping("/runtime")
    public Map<String, String> runtime() {
        return Map.of(
                "camundaBaseUrl", camundaBaseUrl,
                "cirBaseUrl", cirBaseUrl,
                "gmsBaseUrl", gmsBaseUrl
        );
    }

    @GetMapping("/capabilities")
    public List<AutomationCapability> capabilities() {
        return List.of(
                new AutomationCapability("SEND_EMAIL", "Send Email", "INTEGRATION", "automation-workers", "EXTERNAL_TASK", "AVAILABLE"),
                new AutomationCapability("VALIDATE_ADVISORSHIP_REQUEST", "Validate Advisorship Request", "BUSINESS_SERVICE", "advisorship-automation-worker", "EXTERNAL_TASK", "AVAILABLE"),
                new AutomationCapability("REGISTER_ADVISORSHIP", "Register Advisorship", "DOMAIN_SERVICE", "advisorship-automation-worker", "EXTERNAL_TASK", "AVAILABLE"),
                new AutomationCapability("VALIDATE_CPF", "Validate CPF", "BUSINESS_SERVICE", "cpf-service", "REST", "AVAILABLE")
        );
    }

    @PostMapping("/deployments")
    public ResponseEntity<DeploymentResult> deploy(@RequestBody DeploymentRequest request) {
        log.info("ADE deployment request received: projectKey={}, version={}, bpmnXmlBytes={}",
                request.getProjectKey(),
                request.getVersion(),
                request.getBpmnXml() == null ? 0 : request.getBpmnXml().getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        return ResponseEntity.ok(deploymentService.deploy(request));
    }

    @PostMapping("/execution/cir")
    public ResponseEntity<Object> executeCir(@RequestBody Map<String, String> request) {
        String bindingId = request.getOrDefault("bindingId", "ppcomp-main");
        return ResponseEntity.ok(cirExecutionService.execute(bindingId));
    }
}
