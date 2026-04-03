package br.ifes.email_gateway_service.repository;

import br.ifes.email_gateway_service.model.MailBinding;
import br.ifes.email_gateway_service.model.MailBindingConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Component
public class MailBindingRepository {

    public List<MailBinding> findAll() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("config/bindings.json");

            if (is == null) {
                throw new RuntimeException("Arquivo config/bindings.json não encontrado");
            }

            MailBindingConfig config = mapper.readValue(is, MailBindingConfig.class);

            if (config.getBindings() == null) {
                return Collections.emptyList();
            }

            return config.getBindings();

        } catch (Exception e) {
            e.printStackTrace();
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