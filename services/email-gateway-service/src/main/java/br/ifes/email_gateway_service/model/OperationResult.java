package br.ifes.email_gateway_service.model;

/**
 * Representa o resultado simples de uma operação administrativa do GMS.
 */
public class OperationResult {

    private String bindingId;
    private String status;
    private String message;

    public OperationResult() {
    }

    public OperationResult(String bindingId, String status, String message) {
        this.bindingId = bindingId;
        this.status = status;
        this.message = message;
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
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
}