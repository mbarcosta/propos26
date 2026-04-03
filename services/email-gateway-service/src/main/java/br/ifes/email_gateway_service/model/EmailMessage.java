package br.ifes.email_gateway_service.model;

public class EmailMessage {

    private String from;
    private String subject;

    public EmailMessage(String from, String subject) {
        this.from = from;
        this.subject = subject;
    }

    public String getFrom() { return from; }
    public String getSubject() { return subject; }
}