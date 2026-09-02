package br.edu.ifes.deliveryworkers.workers;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class ValidateAdvisorshipRequestWorker {

    private final ExternalTaskClient client;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String ppgManagementBaseUrl;

    public ValidateAdvisorshipRequestWorker(
            ExternalTaskClient client,
            @Value("${ppg.management.base-url}") String ppgManagementBaseUrl) {
        this.client = client;
        this.ppgManagementBaseUrl = ppgManagementBaseUrl;
    }

    @PostConstruct
    public void subscribe() {
        client.subscribe("VALIDATE_ADVISORSHIP_REQUEST")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    List<String> missing = new ArrayList<>();
                    Object studentId = externalTask.getVariable("studentId");
                    Object advisorId = externalTask.getVariable("advisorId");
                    Object requestedStudentName = externalTask.getVariable("studentName");
                    Object requestedAdvisorName = externalTask.getVariable("advisorName");
                    Object requestedStudentEmail = externalTask.getVariable("studentEmail");
                    Object requestedAdvisorEmail = externalTask.getVariable("advisorEmail");
                    Object requester = externalTask.getVariable("from");

                    require(externalTask.getVariable("title"), "title", missing);
                    require(externalTask.getVariable("researchArea"), "researchArea", missing);

                    Map<String, Object> variables = new HashMap<>();
                    ResolvedPerson student = resolvePerson("student", "students", studentId, requestedStudentName, requestedStudentEmail, missing);
                    ResolvedPerson advisor = resolvePerson("advisor", "professors", advisorId, requestedAdvisorName, requestedAdvisorEmail, missing);
                    addResolvedPersonVariables("student", student, variables);
                    addResolvedPersonVariables("advisor", advisor, variables);
                    addProgramVariables(variables, missing);

                    boolean alreadyLinked = student.id() != null
                            && advisor.id() != null
                            && advisorshipExists(student.id(), advisor.id());
                    if (alreadyLinked) {
                        missing.add("existingAdvisorship");
                        variables.put("existingAdvisorship", true);
                    }

                    boolean complete = missing.isEmpty();
                    variables.put("complete", complete);
                    variables.put("missingFields", String.join(",", missing));
                    variables.put("demandValidated", complete);
                    if (!complete) {
                        variables.put("emailTo", stringValue(requester));
                        variables.put("emailSubject", "Pendencia na solicitacao de vinculacao");
                        variables.put("emailBody", feedbackMessage(missing));
                    }

                    externalTaskService.complete(
                            externalTask,
                            variables
                    );
                })
                .open();
    }

    private void require(Object value, String field, List<String> missing) {
        if (value == null || String.valueOf(value).isBlank()) {
            missing.add(field);
        }
    }

    private ResolvedPerson resolvePerson(
            String variablePrefix,
            String endpoint,
            Object id,
            Object name,
            Object email,
            List<String> missing) {
        try {
            Optional<Map<?, ?>> resolved = resolveById(endpoint, id);
            if (resolved.isEmpty()) {
                resolved = resolveByNameOrEmail(endpoint, name, email, variablePrefix, missing);
            }
            if (resolved.isEmpty()) {
                missing.add(variablePrefix + "NotFound");
                return ResolvedPerson.empty();
            }

            ResolvedPerson person = personFrom(resolved.get());
            require(person.name(), variablePrefix + "Name", missing);
            require(person.email(), variablePrefix + "Email", missing);
            return person;
        } catch (Exception e) {
            missing.add(variablePrefix + "Lookup");
            return ResolvedPerson.empty();
        }
    }

    private Optional<Map<?, ?>> resolveById(String endpoint, Object id) {
        if (id == null || String.valueOf(id).isBlank()) {
            return Optional.empty();
        }
        ResponseEntity<Map> response = restTemplate.getForEntity(
                ppgManagementBaseUrl + "/api/" + endpoint + "/" + id,
                Map.class
        );
        return Optional.ofNullable(response.getBody());
    }

    private Optional<Map<?, ?>> resolveByNameOrEmail(
            String endpoint,
            Object name,
            Object email,
            String variablePrefix,
            List<String> missing) {
        String requestedName = stringValue(name);
        String requestedEmail = stringValue(email);
        if (requestedName.isBlank() && requestedEmail.isBlank()) {
            missing.add(variablePrefix + "Identifier");
            return Optional.empty();
        }

        ResponseEntity<List> response = restTemplate.getForEntity(
                ppgManagementBaseUrl + "/api/" + endpoint,
                List.class
        );
        List<?> items = response.getBody() == null ? List.of() : response.getBody();
        List<Map<?, ?>> matches = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Map<?, ?> map && matchesPerson(map, requestedName, requestedEmail)) {
                matches.add(map);
            }
        }

        if (matches.size() > 1) {
            missing.add(variablePrefix + "Ambiguous");
            return Optional.empty();
        }
        return matches.stream().findFirst();
    }

    private boolean matchesPerson(Map<?, ?> item, String requestedName, String requestedEmail) {
        String itemName = stringValue(item.get("name"));
        String itemEmail = stringValue(item.get("email"));
        boolean nameMatches = !requestedName.isBlank() && normalize(itemName).equals(normalize(requestedName));
        boolean emailMatches = !requestedEmail.isBlank() && itemEmail.equalsIgnoreCase(requestedEmail);
        return nameMatches || emailMatches;
    }

    private ResolvedPerson personFrom(Map<?, ?> body) {
        return new ResolvedPerson(
                longValue(body.get("id")),
                stringValue(body.get("name")),
                stringValue(body.get("email"))
        );
    }

    private void addResolvedPersonVariables(String variablePrefix, ResolvedPerson person, Map<String, Object> variables) {
        if (person.id() != null) {
            variables.put(variablePrefix + "Id", person.id());
        }
        if (!person.name().isBlank()) {
            variables.put(variablePrefix + "Name", person.name());
        }
        if (!person.email().isBlank()) {
            variables.put(variablePrefix + "Email", person.email());
        }
    }

    private void addProgramVariables(Map<String, Object> variables, List<String> missing) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    ppgManagementBaseUrl + "/api/programs/default",
                    Map.class
            );
            Map<?, ?> program = response.getBody();
            String programName = program == null ? "" : stringValue(program.get("name"));
            String institution = program == null ? "" : stringValue(program.get("institution"));
            String campus = program == null ? "" : stringValue(program.get("campus"));
            String coordinatorName = program == null ? "" : stringValue(program.get("coordinatorName"));
            String coordinatorEmail = program == null ? "" : stringValue(program.get("coordinatorEmail"));

            require(coordinatorName, "coordinatorName", missing);
            require(coordinatorEmail, "coordinatorEmail", missing);

            variables.put("programName", programName);
            variables.put("institution", institution);
            variables.put("campus", campus);
            variables.put("coordinatorName", coordinatorName);
            variables.put("coordinatorEmail", coordinatorEmail);
        } catch (Exception e) {
            missing.add("programLookup");
        }
    }

    private boolean advisorshipExists(Long studentId, Long advisorId) {
        ResponseEntity<List> response = restTemplate.getForEntity(
                ppgManagementBaseUrl + "/api/advisorships",
                List.class
        );
        List<?> items = response.getBody() == null ? List.of() : response.getBody();
        return items.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .anyMatch(item -> Objects.equals(longValue(item.get("studentId")), studentId)
                        && Objects.equals(longValue(item.get("advisorId")), advisorId));
    }

    private String feedbackMessage(List<String> missing) {
        if (missing.contains("existingAdvisorship")) {
            return "Ja existe uma vinculacao cadastrada para o estudante e orientador informados.";
        }
        return "Nao foi possivel reconhecer completamente a solicitacao de vinculacao. Pendencias: "
                + String.join(", ", missing)
                + ". Informe estudante/orientador por ID, nome exato ou e-mail, alem de titulo e area de pesquisa.";
    }

    private Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(stringValue(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase();
    }

    private record ResolvedPerson(Long id, String name, String email) {
        private static ResolvedPerson empty() {
            return new ResolvedPerson(null, "", "");
        }
    }
}
