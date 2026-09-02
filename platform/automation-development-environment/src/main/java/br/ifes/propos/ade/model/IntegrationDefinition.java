package br.ifes.propos.ade.model;

public class IntegrationDefinition {

    private String channel;
    private String action;
    private String bpmnElementId;
    private String externalEvent;
    private String camundaMessage;
    private String correlationField;
    private String correlationExpression;
    private String variableMappings;
    private String subjectContains;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getBpmnElementId() {
        return bpmnElementId;
    }

    public void setBpmnElementId(String bpmnElementId) {
        this.bpmnElementId = bpmnElementId;
    }

    public String getExternalEvent() {
        return externalEvent;
    }

    public void setExternalEvent(String externalEvent) {
        this.externalEvent = externalEvent;
    }

    public String getCamundaMessage() {
        return camundaMessage;
    }

    public void setCamundaMessage(String camundaMessage) {
        this.camundaMessage = camundaMessage;
    }

    public String getCorrelationField() {
        return correlationField;
    }

    public void setCorrelationField(String correlationField) {
        this.correlationField = correlationField;
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
