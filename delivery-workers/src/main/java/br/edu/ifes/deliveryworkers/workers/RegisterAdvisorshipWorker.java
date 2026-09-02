package br.edu.ifes.deliveryworkers.workers;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class RegisterAdvisorshipWorker {

    private final ExternalTaskClient client;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String ppgManagementBaseUrl;

    public RegisterAdvisorshipWorker(
            ExternalTaskClient client,
            @Value("${ppg.management.base-url}") String ppgManagementBaseUrl) {
        this.client = client;
        this.ppgManagementBaseUrl = ppgManagementBaseUrl;
    }

    @PostConstruct
    public void subscribe() {
        client.subscribe("REGISTER_ADVISORSHIP")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("studentId", longVariable(externalTask.getVariable("studentId")));
                    payload.put("advisorId", longVariable(externalTask.getVariable("advisorId")));
                    payload.put("title", stringVariable(externalTask.getVariable("title"), "Untitled advisorship"));
                    payload.put("researchArea", stringVariable(externalTask.getVariable("researchArea"), "Information Systems"));
                    payload.put("startDate", stringVariable(externalTask.getVariable("startDate"), LocalDate.now().toString()));
                    payload.put("status", "IN_PROGRESS");

                    ResponseEntity<Map> response = restTemplate.postForEntity(
                            ppgManagementBaseUrl + "/api/advisorships",
                            payload,
                            Map.class
                    );

                    Object advisorshipId = response.getBody() == null ? null : response.getBody().get("id");
                    externalTaskService.complete(
                            externalTask,
                            Map.of(
                                    "advisorshipRegistered", true,
                                    "advisorshipId", advisorshipId == null ? "" : String.valueOf(advisorshipId)
                            )
                    );
                })
                .open();
    }

    private Long longVariable(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private String stringVariable(Object value, String defaultValue) {
        if (value == null || String.valueOf(value).isBlank()) {
            return defaultValue;
        }
        return String.valueOf(value);
    }
}

