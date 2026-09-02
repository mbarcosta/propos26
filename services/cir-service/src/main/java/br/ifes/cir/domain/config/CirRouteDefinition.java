package br.ifes.cir.domain.config;

public class CirRouteDefinition {

    private String externalEvent;
    private String action;
    private String messageName;
    private String processDefinitionKey;
    private String businessKeyVariable;
    private String correlationVariable;
    private String correlationExpression;
    private String variableMappings;
    private String subjectContains;

    public String getExternalEvent() {
        return externalEvent;
    }

    public void setExternalEvent(String externalEvent) {
        this.externalEvent = externalEvent;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getBusinessKeyVariable() {
        return businessKeyVariable;
    }

    public void setBusinessKeyVariable(String businessKeyVariable) {
        this.businessKeyVariable = businessKeyVariable;
    }

    public String getCorrelationVariable() {
        return correlationVariable;
    }

    public void setCorrelationVariable(String correlationVariable) {
        this.correlationVariable = correlationVariable;
    }

    public String getCorrelationExpression() {
        return correlationExpression;
    }

    public void setCorrelationExpression(String correlationExpression) {
        this.correlationExpression = correlationExpression;
    }

    public String getVariableMappings() {
        return variableMappings;
    }

    public void setVariableMappings(String variableMappings) {
        this.variableMappings = variableMappings;
    }

    public String getSubjectContains() {
        return subjectContains;
    }

    public void setSubjectContains(String subjectContains) {
        this.subjectContains = subjectContains;
    }
}
