package br.ifes.email_gateway_service.model;

public class MailBinding {

    private String id;
    private boolean active;

    private MailServerConfig mailServer;
    private MailFolderConfig folders;
    private PollingPolicy pollingPolicy;
    private IngestionPolicy ingestionPolicy;

    public MailBinding() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public MailServerConfig getMailServer() {
        return mailServer;
    }

    public void setMailServer(MailServerConfig mailServer) {
        this.mailServer = mailServer;
    }

    public MailFolderConfig getFolders() {
        return folders;
    }

    public void setFolders(MailFolderConfig folders) {
        this.folders = folders;
    }

    public PollingPolicy getPollingPolicy() {
        return pollingPolicy;
    }

    public void setPollingPolicy(PollingPolicy pollingPolicy) {
        this.pollingPolicy = pollingPolicy;
    }

    public IngestionPolicy getIngestionPolicy() {
        return ingestionPolicy;
    }

    public void setIngestionPolicy(IngestionPolicy ingestionPolicy) {
        this.ingestionPolicy = ingestionPolicy;
    }
}