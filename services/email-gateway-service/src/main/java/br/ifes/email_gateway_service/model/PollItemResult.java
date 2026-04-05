package br.ifes.email_gateway_service.model;

/**
 * Representa o resultado da leitura de um único e-mail
 * durante uma operação de polling.
 */
public class PollItemResult {

    /**
     * Remetente do e-mail lido.
     */
    private String from;

    /**
     * Assunto do e-mail lido.
     */
    private String subject;

    /**
     * Status da leitura do e-mail.
     *
     * Exemplos:
     * - READ
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