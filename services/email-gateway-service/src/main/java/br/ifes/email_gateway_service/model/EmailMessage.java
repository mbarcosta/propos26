package br.ifes.email_gateway_service.model;

/**
 * Representa um e-mail simplificado para processamento no sistema.
 */
public class EmailMessage {

    private String from;
    private String subject;
    private String body;
    private int messageNumber;

    public EmailMessage() {
    }

    public EmailMessage(String from, String subject, int messageNumber) {
        this.from = from;
        this.subject = subject;
        this.messageNumber = messageNumber;
    }

    public EmailMessage(String from, String subject, String body, int messageNumber) {
        this.from = from;
        this.subject = subject;
        this.body = body;
        this.messageNumber = messageNumber;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }
}