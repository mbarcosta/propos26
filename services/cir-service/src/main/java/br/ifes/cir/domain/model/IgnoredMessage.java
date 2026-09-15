package br.ifes.cir.domain.model;

public class IgnoredMessage {

    private String messageId;
    private String from;
    private String subject;
    private String reason;

    public IgnoredMessage() {
    }

    public IgnoredMessage(String messageId, String from, String subject, String reason) {
        this.messageId = messageId;
        this.from = from;
        this.subject = subject;
        this.reason = reason;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
