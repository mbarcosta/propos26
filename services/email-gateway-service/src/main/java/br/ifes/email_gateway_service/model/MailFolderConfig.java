package br.ifes.email_gateway_service.model;

public class MailFolderConfig {

    private String inbox;
    private String processed;
    private String error;

    public MailFolderConfig() {
    }

    public String getInbox() {
        return inbox;
    }

    public void setInbox(String inbox) {
        this.inbox = inbox;
    }

    public String getProcessed() {
        return processed;
    }

    public void setProcessed(String processed) {
        this.processed = processed;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}