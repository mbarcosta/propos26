package br.ifes.email_gateway_service.model;

import java.util.List;

public class EmailRule {

    private String name;
    private boolean active;
    private String subject;
    private String actionType;
    private String processKey;
    private List<String> allowedSenderGroups;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getProcessKey() {
        return processKey;
    }

    public void setProcessKey(String processKey) {
        this.processKey = processKey;
    }

    public List<String> getAllowedSenderGroups() {
        return allowedSenderGroups;
    }

    public void setAllowedSenderGroups(List<String> allowedSenderGroups) {
        this.allowedSenderGroups = allowedSenderGroups;
    }
}