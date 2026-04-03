package br.ifes.email_gateway_service.config;

import br.ifes.email_gateway_service.model.EmailRule;
import br.ifes.email_gateway_service.model.EmailRuleConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class RuleLoader {

    public List<EmailRule> loadRules() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("config/email-rules.json");

            if (is == null) {
                throw new RuntimeException("Arquivo config/email-rules.json não encontrado");
            }

            EmailRuleConfig config = mapper.readValue(is, EmailRuleConfig.class);

            return config.getRules();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao carregar regras", e);
        }
    }
}