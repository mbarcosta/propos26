package br.ifes.email_gateway_service.api.dto;

/**
 * DTO de entrada usado pelo endpoint que confirma que uma mensagem
 * foi processada com sucesso por um sistema cliente, como o CIR.
 *
 * <p>A mensagem é identificada por seu {@code messageId}, que é
 * mais adequado para integração entre serviços do que um identificador
 * operacional interno de pasta IMAP.</p>
 */
public class MarkAsProcessedRequest {

    /**
     * Identificador técnico da mensagem de e-mail.
     */
    private String messageId;

    public MarkAsProcessedRequest() {
    }

    public MarkAsProcessedRequest(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Retorna o identificador da mensagem.
     *
     * @return identificador técnico da mensagem
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Define o identificador da mensagem.
     *
     * @param messageId identificador técnico da mensagem
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
