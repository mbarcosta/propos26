package br.ifes.propos.ade.model;

public class DeploymentResult {

    private String status;
    private String message;
    private String deploymentId;
    private String deploymentName;

    public static DeploymentResult ready(String deploymentId, String deploymentName) {
        DeploymentResult result = new DeploymentResult();
        result.setStatus("READY");
        result.setMessage("BPMN deployed to Camunda 7");
        result.setDeploymentId(deploymentId);
        result.setDeploymentName(deploymentName);
        return result;
    }

    public static DeploymentResult failed(String message) {
        DeploymentResult result = new DeploymentResult();
        result.setStatus("FAILED");
        result.setMessage(message);
        return result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }
}

