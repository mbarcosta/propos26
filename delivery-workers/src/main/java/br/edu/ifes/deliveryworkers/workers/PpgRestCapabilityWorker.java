package br.edu.ifes.deliveryworkers.workers;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PpgRestCapabilityWorker {

    private final ExternalTaskClient client;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
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
        client.subscribe("FIND_ADVISORSHIP_BY_STUDENT")
                .lockDuration(10000)
                .handler(this::handleFindAdvisorshipByStudent)
                .open();
        client.subscribe("UPLOAD_DISSERTATION")
                .lockDuration(30000)
                .handler(this::handleUploadDissertation)
                .open();
    }

    private void handleUploadDissertation(ExternalTask task, ExternalTaskService service) {
        try {
            Long defenseId = longVariable(task.getVariable("defenseId"));
            if (defenseId == null) {
                fail(task, service, "UPLOAD_DISSERTATION", "defenseId is required.");
                return;
            }
            Resource file = dissertationResource(task);
            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add("file", file);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    ppgManagementBaseUrl + "/api/defenses/" + defenseId + "/dissertation",
                    new HttpEntity<>(form, headers), Map.class);
            Map<?, ?> body = response.getBody();
            Map<String, Object> variables = new LinkedHashMap<>();
            if (body != null) {
                for (String field : List.of("documentId", "defenseId", "fileName", "contentType", "size", "version", "uploadedAt")) {
                    copy(body, variables, field, field);
                }
            }
            variables.put("dissertationUploaded", true);
            service.complete(task, variables);
        } catch (Exception e) {
            fail(task, service, "UPLOAD_DISSERTATION", e.getMessage());
        }
    }

    private Resource dissertationResource(ExternalTask task) throws Exception {
        String fileName = firstNonBlank(task.getVariable("fileName"), "dissertation.pdf");
        String base64 = firstNonBlank(task.getVariable("fileContentBase64"), task.getVariable("file"));
        if (!base64.isBlank()) {
            if (base64.contains(",") && base64.substring(0, base64.indexOf(',')).contains("base64")) {
                base64 = base64.substring(base64.indexOf(',') + 1);
            }
            byte[] content = Base64.getMimeDecoder().decode(base64);
            return new ByteArrayResource(content) {
                @Override public String getFilename() { return fileName; }
            };
        }
        // ExternalTask#getVariable is generic. In a single varargs call Java can
        // infer Object[] and insert a runtime cast, although Camunda returns a
        // String. Force Object here so the path is handled as one value.
        String filePath = firstNonBlank((Object) task.getVariable("filePath"));
        if (!filePath.isBlank()) {
            Path path = Path.of(filePath).normalize();
            if (!Files.isRegularFile(path)) throw new IllegalArgumentException("filePath does not identify a readable file: " + filePath);
            return new FileSystemResource(path) {
                @Override public String getFilename() { return fileName; }
            };
        }
        throw new IllegalArgumentException("File content not found. Provide fileContentBase64/file and fileName, or filePath accessible to the worker.");
    }

    private void handleFindAdvisorshipByStudent(ExternalTask externalTask,
                                                 ExternalTaskService externalTaskService) {
        try {
            Long studentId = longVariable(externalTask.getVariable("studentId"));
            if (studentId == null) {
                fail(externalTask, externalTaskService, "FIND_ADVISORSHIP_BY_STUDENT", "studentId is required.");
                return;
            }

            ResponseEntity<List> response = restTemplate.getForEntity(
                    ppgManagementBaseUrl + "/api/advisorships/by-student/" + studentId,
                    List.class
            );
            List<?> advisorships = response.getBody();
            Map<?, ?> advisorship = advisorships == null ? null : advisorships.stream()
                    .filter(Map.class::isInstance)
                    .map(Map.class::cast)
                    .filter(item -> isCurrentAdvisorship(item.get("status")))
                    .findFirst()
                    .orElse(null);
            if (advisorship == null) {
                fail(externalTask, externalTaskService, "FIND_ADVISORSHIP_BY_STUDENT",
                        "No active advisorship found for studentId " + studentId);
                return;
            }

            Map<String, Object> variables = new LinkedHashMap<>();
            copy(advisorship, variables, "id", "advisorshipId");
            copy(advisorship, variables, "id", "id");
            copy(advisorship, variables, "studentId", "studentId");
            copy(advisorship, variables, "advisorId", "advisorId");
            copy(advisorship, variables, "status", "advisorshipStatus");
            copy(advisorship, variables, "title", "advisorshipTitle");
            copy(advisorship, variables, "researchArea", "researchArea");
            System.out.println("=== PPG REST capability ===");
            System.out.println("Topic: FIND_ADVISORSHIP_BY_STUDENT");
            System.out.println("Input: " + studentId);
            System.out.println("Outputs: " + variables.keySet());
            externalTaskService.complete(externalTask, variables);
        } catch (Exception e) {
            fail(externalTask, externalTaskService, "FIND_ADVISORSHIP_BY_STUDENT", e.getMessage());
        }
    }

    private boolean isCurrentAdvisorship(Object status) {
        String value = status == null ? "" : String.valueOf(status).trim();
        return "ACTIVE".equalsIgnoreCase(value) || "IN_PROGRESS".equalsIgnoreCase(value);
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
        if (value instanceof String text && !text.isBlank()) {
            try {
                return objectMapper.readValue(text, List.class);
            } catch (Exception ignored) {
                return new ArrayList<>();
            }
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
