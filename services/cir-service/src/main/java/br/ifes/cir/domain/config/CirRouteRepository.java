package br.ifes.cir.domain.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class CirRouteRepository {

    private final Path routesFile;
    private final ObjectMapper objectMapper;

    public CirRouteRepository(
            @Value("${cir.routes-file:data/routes.json}") String routesFile,
            ObjectMapper objectMapper) {
        this.routesFile = Path.of(routesFile);
        this.objectMapper = objectMapper;
    }

    public synchronized List<CirRouteDefinition> findAll() {
        return load().getRoutes();
    }

    public synchronized void replaceAll(List<CirRouteDefinition> routes) {
        CirRouteConfig config = new CirRouteConfig();
        config.setRoutes(routes);
        save(config);
    }

    public synchronized Optional<CirRouteDefinition> findStartRouteBySubject(String subject) {
        String normalized = normalize(subject);
        return findAll().stream()
                .filter(route -> "START_PROCESS".equalsIgnoreCase(route.getAction()))
                .filter(route -> route.getSubjectContains() != null)
                .filter(route -> normalized.contains(normalize(route.getSubjectContains())))
                .findFirst();
    }

    public synchronized Optional<CirRouteDefinition> findReplyRoute(String subject, String body) {
        String text = normalize(subject) + "\n" + normalize(body);
        List<CirRouteDefinition> replyRoutes = findAll().stream()
                .filter(route -> "CORRELATE_MESSAGE".equalsIgnoreCase(route.getAction()))
                .filter(route -> route.getExternalEvent() != null)
                .toList();

        Optional<CirRouteDefinition> exactRoute = replyRoutes.stream()
                .filter(route -> text.contains(normalize(route.getExternalEvent()))
                        || text.contains(normalize(route.getMessageName()))
                        || text.contains(normalize(route.getExternalEvent()).replace("_", " ")))
                .findFirst();
        if (exactRoute.isPresent()) {
            return exactRoute;
        }

        if (text.contains("dados complementares") || text.contains("pendencia")) {
            return findReplyRouteByExternalEvent(replyRoutes, "DADOS_COMPLEMENTARES");
        }
        if (text.contains("confirmacao final") || text.contains("coordenador")) {
            return findReplyRouteByExternalEvent(replyRoutes, "CONFIRMACAO_COORDENADOR");
        }
        if (text.contains("confirmacao de vinculacao") || text.contains("confirmo")) {
            return findReplyRouteByExternalEvent(replyRoutes, "CONFIRMACAO_ESTUDANTE");
        }

        return replyRoutes.stream()
                .filter(route -> "EMAIL_REPLY".equalsIgnoreCase(route.getExternalEvent())
                        || "EMAIL_REPLY".equalsIgnoreCase(route.getMessageName()))
                .findFirst();
    }

    private Optional<CirRouteDefinition> findReplyRouteByExternalEvent(
            List<CirRouteDefinition> routes,
            String externalEvent) {
        return routes.stream()
                .filter(route -> externalEvent.equalsIgnoreCase(route.getExternalEvent()))
                .findFirst();
    }

    private CirRouteConfig load() {
        try {
            if (!Files.exists(routesFile)) {
                Files.createDirectories(routesFile.toAbsolutePath().getParent());
                CirRouteConfig config = defaultConfig();
                save(config);
                return config;
            }
            return objectMapper.readValue(routesFile.toFile(), CirRouteConfig.class);
        } catch (Exception e) {
            throw new IllegalStateException("Could not load CIR routes from " + routesFile, e);
        }
    }

    private void save(CirRouteConfig config) {
        try {
            Files.createDirectories(routesFile.toAbsolutePath().getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(routesFile.toFile(), config);
        } catch (Exception e) {
            throw new IllegalStateException("Could not save CIR routes to " + routesFile, e);
        }
    }

    private CirRouteConfig defaultConfig() {
        CirRouteDefinition start = new CirRouteDefinition();
        start.setExternalEvent("VINCULACAO_SOLICITADA");
        start.setAction("START_PROCESS");
        start.setMessageName("VINCULACAO_SOLICITADA");
        start.setProcessDefinitionKey("vinculacao_orientacao");
        start.setBusinessKeyVariable("requestId");
        start.setCorrelationVariable("requestId");
        start.setSubjectContains("vinculacao");

        CirRouteDefinition reply = new CirRouteDefinition();
        reply.setExternalEvent("EMAIL_REPLY");
        reply.setAction("CORRELATE_MESSAGE");
        reply.setMessageName("EMAIL_REPLY");
        reply.setCorrelationVariable("correlationId");

        CirRouteConfig config = new CirRouteConfig();
        config.setRoutes(List.of(start, reply));
        return config;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT);
    }
}
