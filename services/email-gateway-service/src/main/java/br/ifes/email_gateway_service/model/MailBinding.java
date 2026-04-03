package br.ifes.email_gateway_service.model;

import java.util.List;

/**
 * Representa uma configuração de acesso a uma caixa postal (binding),
 * incluindo parâmetros de conexão IMAP e regras de processamento associadas.
 *
 * Um MailBinding define:
 * - como acessar uma caixa de e-mail (host, porta, credenciais)
 * - quais mensagens devem ser lidas (pasta origem)
 * - o destino das mensagens processadas (pasta de processados)
 * - quais regras devem ser aplicadas para classificar e processar os e-mails
 *
 * Esse conceito permite que o serviço seja reutilizável para múltiplas
 * caixas postais, cada uma com seu próprio conjunto de regras.
 */
public class MailBinding {

    /**
     * Identificador único do binding.
     * Usado para invocação via API (ex: /bindings/{id}/poll).
     */
    private String id;

    /**
     * Nome descritivo do binding.
     * Usado apenas para fins de identificação humana.
     */
    private String name;

    /**
     * Indica se o binding está ativo.
     * Bindings inativos não devem ser processados.
     */
    private boolean active;

    /**
     * Host do servidor IMAP (ex: imap.gmail.com).
     */
    private String imapHost;

    /**
     * Porta do servidor IMAP (ex: 993 para IMAPS).
     */
    private int imapPort;

    /**
     * Endereço de e-mail da caixa postal.
     */
    private String mailboxAddress;

    /**
     * Credencial de acesso à caixa postal.
     * Idealmente deve ser uma app password (no caso de Gmail).
     *
     * ⚠️ Observação:
     * Em versões futuras, este campo deve ser substituído por um
     * credentialRef para armazenamento seguro.
     */
    private String appPassword;

    /**
     * Pasta de origem dos e-mails a serem lidos.
     * Normalmente "INBOX".
     */
    private String sourceFolder;

    /**
     * Pasta de destino para e-mails processados com sucesso.
     * Exemplo: "Processed".
     */
    private String processedFolder;

    /**
     * Lista de regras associadas a este binding.
     * Cada regra define critérios de reconhecimento e ação.
     */
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

    public List<EmailRule> getRules() {
        return rules;
    }

    public void setRules(List<EmailRule> rules) {
        this.rules = rules;
    }
}