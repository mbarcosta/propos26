package br.ifes.cir.client.dto;

/**
 * DTO que representa o resultado resumido do processamento
 * de um e-mail lido pelo GMS.
 */
public class GmsReadResult {

    /**
     * Remetente do e-mail.
     */
    private String from;

    /**
     * Assunto do e-mail.
     */
    private String subject;

    /**
     * Status do processamento do e-mail.
     */
    private String status;

    /**
     * Mensagem descritiva do resultado.
     */
    private String message;

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