package br.ifes.propos.ade.model;

public class DeploymentRequest {

    private String projectKey;
    private String version;
    private String bpmnXml;
    private IntegrationDefinition integration;
    private java.util.List<IntegrationDefinition> inboundIntegrations;

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBpmnXml() {
        return bpmnXml;
    }

    public void setBpmnXml(String bpmnXml) {
        this.bpmnXml = bpmnXml;
    }

    public IntegrationDefinition getIntegration() {
        return integration;
    }

    public void setIntegration(IntegrationDefinition integration) {
        this.integration = integration;
    }

    public java.util.List<IntegrationDefinition> getInboundIntegrations() {
        return inboundIntegrations;
    }

    public void setInboundIntegrations(java.util.List<IntegrationDefinition> inboundIntegrations) {
        this.inboundIntegrations = inboundIntegrations;
    }
}
