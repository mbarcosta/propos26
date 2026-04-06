package br.ifes.cir.domain.model;

/**
 * Representa um evento de negócio identificado pelo CIR
 * a partir de uma mensagem retornada pelo GMS.
 *
 * <p>Enquanto o GMS retorna mensagens lidas, o CIR passa a
 * interpretá-las semanticamente, classificando-as em eventos
 * de negócio mais úteis para a orquestração.</p>
 */
public class CirIdentifiedEvent {

    /**
     * Identificador técnico da mensagem de origem.
     */
    private String messageId;

    /**
     * Tipo do evento de negócio identificado.
     *
     * <p>Exemplo: VINCULACAO_RECEBIDA.</p>
     */
    private String eventType;

    /**
     * Remetente da mensagem.
     */
    private String from;

    /**
     * Assunto da mensagem.
     */
    private String subject;

    /**
     * Indica se a mensagem possui anexos.
     */
    private boolean hasAttachments;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
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

    public boolean isHasAttachments() {
        return hasAttachments;
    }

    public void setHasAttachments(boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }
}