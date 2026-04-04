package br.ifes.email_gateway_service.service;

import br.ifes.email_gateway_service.model.EmailMessage;
import br.ifes.email_gateway_service.model.EmailRule;
import br.ifes.email_gateway_service.model.MailBinding;
import br.ifes.email_gateway_service.model.SenderGroup;
import br.ifes.email_gateway_service.model.SenderGroupType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleMatcher {

    public EmailRule match(EmailMessage email, MailBinding binding) {

        for (EmailRule rule : binding.getRules()) {
            if (!rule.isActive()) {
                continue;
            }

            String emailSubject = normalize(email.getSubject());
            String ruleSubject = normalize(rule.getSubject());

            if (!emailSubject.equals(ruleSubject)) {
                continue;
            }

            if (!matchesSenderGroups(email.getFrom(), rule, binding.getSenderGroups())) {
                continue;
            }

            return rule;
        }

        return null;
    }

    private boolean matchesSenderGroups(String sender,
                                        EmailRule rule,
                                        List<SenderGroup> availableGroups) {

        if (rule.getAllowedSenderGroups() == null || rule.getAllowedSenderGroups().isEmpty()) {
            return false;
        }

        for (String groupName : rule.getAllowedSenderGroups()) {
            SenderGroup group = findGroupByName(groupName, availableGroups);

            if (group != null && matchesGroup(sender, group)) {
                return true;
            }
        }

        return false;
    }

    private SenderGroup findGroupByName(String groupName, List<SenderGroup> groups) {
        if (groups == null) {
            return null;
        }

        for (SenderGroup group : groups) {
            if (normalize(group.getName()).equals(normalize(groupName))) {
                return group;
            }
        }

        return null;
    }

    private boolean matchesGroup(String sender, SenderGroup group) {
        if (group.getType() == null) {
            return false;
        }

        return switch (group.getType()) {
            case ANY -> true;
            case EXACT_LIST -> matchesExactList(sender, group.getSenders());
            case DOMAIN -> matchesDomain(sender, group.getDomains());
        };
    }

    private boolean matchesExactList(String sender, List<String> senders) {
        if (senders == null) {
            return false;
        }

        String normalizedSender = normalizeEmail(sender);

        for (String allowed : senders) {
            if (normalizedSender.equals(normalizeEmail(allowed))) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesDomain(String sender, List<String> domains) {
        if (domains == null) {
            return false;
        }

        String normalizedSender = normalizeEmail(sender);
        int at = normalizedSender.indexOf("@");

        if (at < 0 || at == normalizedSender.length() - 1) {
            return false;
        }

        String senderDomain = normalizedSender.substring(at + 1);

        for (String domain : domains) {
            if (senderDomain.equals(normalizeDomain(domain))) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private String normalizeEmail(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String normalizeDomain(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim().toLowerCase();
        return normalized.startsWith("@") ? normalized.substring(1) : normalized;
    }
}