package br.ifes.email_gateway_service.model;

import java.util.List;

public class EmailRuleConfig {

    private List<EmailRule> rules;

    public List<EmailRule> getRules() { return rules; }
    public void setRules(List<EmailRule> rules) { this.rules = rules; }
}