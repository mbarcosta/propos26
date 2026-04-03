package br.ifes.email_gateway_service.service;

import br.ifes.email_gateway_service.model.EmailMessage;
import br.ifes.email_gateway_service.model.EmailRule;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleMatcher {

    public EmailRule match(EmailMessage email, List<EmailRule> rules) {
          
        for (EmailRule rule : rules) {
        	String emailSubject = normalize(email.getSubject());
        	String ruleSubject = normalize(rule.getSubject());
            if (!rule.isActive()) continue;

            if (!emailSubject.equals(ruleSubject)) continue;

            if (!rule.getAuthorizedSenders().contains(email.getFrom())) continue;

            return rule;
        }

        return null;
    }
    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}