package br.ifes.cir.client.dto;

import java.util.List;

/**
 * DTO que representa uma mensagem de e-mail retornada
 * pelo GMS com maior nível de detalhe.
 */
public class GmsMessage {

    /**
     * Identificador técnico da mensagem.
     */
    private String messageId;

    /**
     * Número sequencial atribuído à mensagem no contexto do processamento.
     */
    private Integer messageNumber;

    /**
     * Remetente da mensagem.
     */
    private String from;

    /**
     * Lista de destinatários principais.
     */
    private List<String> to;

    /**
     * Lista de destinatários em cópia.
     */
    private List<String> cc;

    /**
     * Assunto da mensagem.
     */
    private String subject;

    /**
     * Corpo textual da mensagem.
     */
    private String body;

    /**
     * Data/hora de recebimento da mensagem.
     *
     * <p>Nesta primeira versão será mantido como String para evitar
     * atrito desnecessário com parsing de data.</p>
     */
    private String receivedAt;

    /**
     * Campo de cabeçalho In-Reply-To, quando existir.
     */
    private String inReplyTo;

    /**
     * Lista de referências da mensagem, quando existir.
     */
    private List<String> references;

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

    public Integer getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(Integer messageNumber) {
        this.messageNumber = messageNumber;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
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

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getInReplyTo() {
        return inReplyTo;
    }

    public void setInReplyTo(String inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public List<String> getReferences() {
        return references;
    }

    public void setReferences(List<String> references) {
        this.references = references;
    }

    public boolean isHasAttachments() {
        return hasAttachments;
    }

    public void setHasAttachments(boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }
}