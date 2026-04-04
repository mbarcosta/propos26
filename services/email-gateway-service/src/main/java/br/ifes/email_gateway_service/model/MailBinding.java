package br.ifes.email_gateway_service.model;

import java.util.List;

/**
 * Representa a configuração de integração de um cliente com o gateway.
 */
public class MailBinding {

    private String id;
    private String name;
    private boolean active;
    private String imapHost;
    private int imapPort;
    private String mailboxAddress;
    private String appPassword;
    private String sourceFolder;
    private String processedFolder;
    private List<SenderGroup> senderGroups;
    private List<EmailRule> rules;

    public MailBinding() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getImapHost() {
        return imapHost;
    }

    public void setImapHost(String imapHost) {
        this.imapHost = imapHost;
    }

    public int getImapPort() {
        return imapPort;
    }

    public void setImapPort(int imapPort) {
        this.imapPort = imapPort;
    }

    public String getMailboxAddress() {
        return mailboxAddress;
    }

    public void setMailboxAddress(String mailboxAddress) {
        this.mailboxAddress = mailboxAddress;
    }

    public String getAppPassword() {
        return appPassword;
    }

    public void setAppPassword(String appPassword) {
        this.appPassword = appPassword;
    }

    public String getSourceFolder() {
        return sourceFolder;
    }

    public void setSourceFolder(String sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    public String getProcessedFolder() {
        return processedFolder;
    }

    public void setProcessedFolder(String processedFolder) {
        this.processedFolder = processedFolder;
    }

    public List<SenderGroup> getSenderGroups() {
        return senderGroups;
    }

    public void setSenderGroups(List<SenderGroup> senderGroups) {
        this.senderGroups = senderGroups;
    }

    public List<EmailRule> getRules() {
        return rules;
    }

    public void setRules(List<EmailRule> rules) {
        this.rules = rules;
    }
}