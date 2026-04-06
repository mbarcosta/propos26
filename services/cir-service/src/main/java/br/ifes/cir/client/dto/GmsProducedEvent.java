package br.ifes.cir.client.dto;

/**
 * DTO que representa um evento individual produzido pelo GMS.
 *
 * <p>Cada evento corresponde, em geral, a uma interpretação feita pelo GMS
 * a partir de um e-mail processado. O CIR usará essas informações para decidir
 * qual ação tomar futuramente, por exemplo:
 * <ul>
 *   <li>iniciar um processo no Camunda</li>
 *   <li>correlacionar uma mensagem</li>
 *   <li>ignorar o evento</li>
 * </ul>
 * </p>
 *
 * <p>Nesta fase inicial, capturamos apenas alguns campos essenciais.
 * Caso o JSON do GMS contenha mais atributos relevantes, esta classe pode
 * ser estendida depois.</p>
 */
public class GmsProducedEvent {

    /**
     * Tipo do evento produzido pelo GMS.
     *
     * <p>Exemplo hipotético: VINCULACAO_RECEBIDA.</p>
     */
    private String eventType;

    /**
     * Assunto do e-mail que originou o evento.
     */
    private String subject;

    /**
     * Remetente do e-mail que originou o evento.
     */
    private String sender;

    /**
     * Retorna o tipo do evento.
     *
     * @return tipo do evento
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Define o tipo do evento.
     *
     * @param eventType tipo do evento
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * Retorna o assunto do e-mail associado ao evento.
     *
     * @return assunto do e-mail
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Define o assunto do e-mail associado ao evento.
     *
     * @param subject assunto do e-mail
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Retorna o remetente do e-mail associado ao evento.
     *
     * @return remetente do e-mail
     */
    public String getSender() {
        return sender;
    }

    /**
     * Define o remetente do e-mail associado ao evento.
     *
     * @param sender remetente do e-mail
     */
    public void setSender(String sender) {
        this.sender = sender;
    }
}