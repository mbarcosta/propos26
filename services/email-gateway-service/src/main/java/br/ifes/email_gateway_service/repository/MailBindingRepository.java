package br.ifes.email_gateway_service.repository;

import br.ifes.email_gateway_service.model.MailBinding;
import br.ifes.email_gateway_service.model.MailBindingConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Component
public class MailBindingRepository {

    private static final Logger log = LoggerFactory.getLogger(MailBindingRepository.class);

    private final String bindingsFile;
    private final ObjectMapper objectMapper;

    public MailBindingRepository(
            @Value("${app.bindings.file:data/bindings.json}") String bindingsFile,
            ObjectMapper objectMapper) {
        this.bindingsFile = bindingsFile;
        this.objectMapper = objectMapper;
    }

    public List<MailBinding> findAll() {
        try {
            Path path = Paths.get(bindingsFile).toAbsolutePath();

            log.info("Loading bindings from: {}", path);

            if (!Files.exists(path)) {
                throw new RuntimeException("Arquivo de bindings não encontrado: " + path);
            }

            MailBindingConfig config = objectMapper.readValue(path.toFile(), MailBindingConfig.class);

            if (config == null || config.getBindings() == null) {
                return Collections.emptyList();
            }

            return config.getBindings();

        } catch (Exception e) {
            log.error("Erro ao carregar bindings", e);
            throw new RuntimeException("Erro ao carregar bindings", e);
        }
    }

    public MailBinding findById(String bindingId) {
        return findAll().stream()
                .filter(binding -> bindingId.equals(binding.getId()))
                .findFirst()
                .orElse(null);
    }
}