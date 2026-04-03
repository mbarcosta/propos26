package br.ifes.email_gateway_service.model;

/**
 * Representa o resultado do processamento de um único e-mail
 * durante uma operação de polling.
 */
public class PollItemResult {

    /**
     * Remetente do e-mail processado.
     */
    private String from;

    /**
     * Assunto do e-mail processado.
     */
    private String subject;

    /**
     * Nome da regra aplicada, se houver.
     */
    private String ruleName;

    /**
     * Status do processamento do e-mail.
     *
     * Exemplos:
     * - PROCESSED
     * - IGNORED
     * - ERROR
     */
    private String status;

    /**
     * Mensagem complementar de diagnóstico.
     */
    private String message;

    public PollItemResult() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
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
