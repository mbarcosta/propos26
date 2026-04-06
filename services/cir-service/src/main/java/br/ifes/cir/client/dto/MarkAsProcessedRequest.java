package br.ifes.cir.client.dto;

/**
 * DTO enviado pelo CIR ao GMS para confirmar que uma mensagem
 * foi processada com sucesso e pode ser movida para "Processed".
 */
public class MarkAsProcessedRequest {

    private String messageId;

    public MarkAsProcessedRequest() {
    }

    public MarkAsProcessedRequest(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}