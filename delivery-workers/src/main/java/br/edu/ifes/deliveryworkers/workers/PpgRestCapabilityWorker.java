package br.edu.ifes.deliveryworkers.workers;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PpgRestCapabilityWorker {

    private final ExternalTaskClient client;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String ppgManagementBaseUrl;

    private final List<CapabilityRoute> routes = List.of(
            new CapabilityRoute("FIND_STUDENT", "/api/students/{studentId}", "student", List.of("studentId", "id")),
            new CapabilityRoute("FIND_STUDENT_BY_REGISTRATION", "/api/students/by-registration/{registration}", "student", List.of("registration", "studentRegistration")),
            new CapabilityRoute("FIND_STUDENT_BY_EMAIL", "/api/students/by-email?email={email}", "student", List.of("studentEmail", "email")),
            new CapabilityRoute("FIND_STUDENT_BY_NAME", "/api/students/by-name?name={name}", "student", List.of("studentName", "name")),
            new CapabilityRoute("FIND_PROFESSOR", "/api/professors/{professorId}", "advisor", List.of("advisorId", "professorId", "id")),
            new CapabilityRoute("FIND_PROFESSOR_BY_EMAIL", "/api/professors/by-email?email={email}", "advisor", List.of("advisorEmail", "professorEmail", "email")),
            new CapabilityRoute("FIND_ADVISOR_BY_NAME", "/api/professors/by-name?name={name}", "advisor", List.of("advisorName", "professorName", "name"))
    );

    public PpgRestCapabilityWorker(
            ExternalTaskClient client,
            @Value("${ppg.management.base-url}") String ppgManagementBaseUrl) {
        this.client = client;
        this.ppgManagementBaseUrl = ppgManagementBaseUrl;
    }

    @PostConstruct
    public void subscribe() {
        routes.forEach(route -> client.subscribe(route.topic())
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> handle(route, externalTask, externalTaskService))
                .open());
        client.subscribe("CREATE_DEFENSE")
                .lockDuration(10000)
                .handler(this::handleCreateDefense)
                .open();
    }

    private void handle(CapabilityRoute route, ExternalTask externalTask, ExternalTaskService externalTaskService) {
        try {
            String input = firstNonBlank(route.inputCandidates().stream()
                    .map(externalTask::<Object>getVariable)
                    .toArray());
            if (input.isBlank()) {
                fail(externalTask, externalTaskService, route.topic(), "Input not found. Expected one of " + route.inputCandidates());
                return;
            }

            String url = ppgManagementBaseUrl + route.pathTemplate().replace(route.placeholder(), encode(input));
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<?, ?> body = response.getBody();
            if (body == null || body.isEmpty()) {
                fail(externalTask, externalTaskService, route.topic(), "PPG Management returned no data for " + route.inputCandidates().get(0));
                return;
            }

            Map<String, Object> variables = outputVariables(route.prefix(), body);
            System.out.println("=== PPG REST capability ===");
            System.out.println("Topic: " + route.topic());
            System.out.println("Input: " + input);
            System.out.println("Outputs: " + variables.keySet());
            externalTaskService.complete(externalTask, variables);
        } catch (Exception e) {
            fail(externalTask, externalTaskService, route.topic(), e.getMessage());
        }
    }

    private void handleCreateDefense(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        try {
            Long studentId = longVariable(externalTask.getVariable("studentId"));
            Long advisorId = longVariable(firstNonBlank(
                    externalTask.getVariable("advisorId"),
                    externalTask.getVariable("professorId")));
            if (studentId == null || advisorId == null) {
                fail(externalTask, externalTaskService, "CREATE_DEFENSE", "studentId and advisorId are required.");
                return;
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("studentId", studentId);
            payload.put("advisorId", advisorId);
            payload.put("title", firstNonBlank(externalTask.getVariable("title"), "Untitled defense"));
            payload.put("date", firstNonBlank(
                    externalTask.getVariable("date"),
                    externalTask.getVariable("scheduledAt"),
                    LocalDate.now().toString()));
            payload.put("location", firstNonBlank(externalTask.getVariable("location"), "A definir"));
            payload.put("committeeMembers", committeeMembers(externalTask.getVariable("committeeMembers")));

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    ppgManagementBaseUrl + "/api/defenses",
                    payload,
                    Map.class
            );
            Map<?, ?> body = response.getBody();
            Map<String, Object> variables = new LinkedHashMap<>();
            if (body != null) {
                copy(body, variables, "id", "defenseId");
                copy(body, variables, "status", "defenseStatus");
                copy(body, variables, "title", "defenseTitle");
                copy(body, variables, "date", "defenseDate");
                copy(body, variables, "location", "defenseLocation");
                copy(body, variables, "id", "id");
                copy(body, variables, "status", "status");
            }
            variables.put("defenseCreated", true);
            externalTaskService.complete(externalTask, variables);
        } catch (Exception e) {
            fail(externalTask, externalTaskService, "CREATE_DEFENSE", e.getMessage());
        }
    }

    private Map<String, Object> outputVariables(String prefix, Map<?, ?> body) {
        Map<String, Object> variables = new LinkedHashMap<>();
        copy(body, variables, "id", prefix + "Id");
        copy(body, variables, "registration", prefix + "Registration");
        copy(body, variables, "name", prefix + "Name");
        copy(body, variables, "email", prefix + "Email");
        copy(body, variables, "status", prefix + "Status");
        copy(body, variables, "id", "id");
        copy(body, variables, "name", "name");
        copy(body, variables, "email", "email");
        return variables;
    }

    private void copy(Map<?, ?> source, Map<String, Object> target, String sourceName, String targetName) {
        Object value = source.get(sourceName);
        if (value != null && !String.valueOf(value).isBlank()) {
            target.put(targetName, value);
        }
    }

    private String firstNonBlank(Object... values) {
        for (Object value : values) {
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value).trim();
            }
        }
        return "";
    }

    private Long longVariable(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value).trim());
    }

    private Object committeeMembers(Object value) {
        if (value instanceof List<?> list) {
            return list;
        }
        return new ArrayList<>();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void fail(ExternalTask externalTask, ExternalTaskService externalTaskService, String topic, String message) {
        externalTaskService.handleFailure(
                externalTask,
                topic + " failed",
                message == null ? "Unknown error" : message,
                nextRetries(externalTask),
                60000
        );
    }

    private int nextRetries(ExternalTask externalTask) {
        Integer retries = externalTask.getRetries();
        if (retries == null) {
            return 3;
        }
        return Math.max(retries - 1, 0);
    }

    private record CapabilityRoute(String topic, String pathTemplate, String prefix, List<String> inputCandidates) {
        String placeholder() {
            int start = pathTemplate.indexOf('{');
            int end = pathTemplate.indexOf('}', start);
            return start >= 0 && end > start ? pathTemplate.substring(start, end + 1) : "";
        }
    }
}
