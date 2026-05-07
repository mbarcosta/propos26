package br.ifes.cir.domain.rule;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa o resultado da classificação de uma mensagem de e-mail
 * realizada pelo CIR.
 *
 * <p>Este objeto é o contrato interno entre o classificador e o
 * {@code CirService}. Ele contém todas as informações necessárias
 * para decidir como a mensagem será encaminhada ao Camunda.</p>
 *
 * <p>Ele substitui o uso de eventos específicos (como Vinculação),
 * permitindo que o CIR opere de forma genérica.</p>
 */
public class ClassifiedMessage {

    /**
     * Tipo lógico da mensagem (START, REPLY, IRRELEVANT).
     */
    private MessageClassificationKind kind;

    /**
     * Nome da mensagem BPMN a ser enviada ao Camunda.
     *
     * <p>Exemplo:
     * - VINCULACAO_START
     * - DEFESA_START
     * - EMAIL_REPLY</p>
     */
    private String messageName;

    /**
     * Identificador de correlação extraído da mensagem.
     *
     * <p>Usado apenas para REPLY.</p>
     */
    private String correlationId;

    /**
     * Identificador único da mensagem (IMAP/GMS).
     */
    private String messageId;

    /**
     * Remetente do e-mail.
     */
    private String from;

    /**
     * Assunto do e-mail.
     *
     * <p>Importante: deve ser preservado para análise no processo.</p>
     */
    private String subject;

    /**
     * Corpo do e-mail.
     */
    private String body;

    /**
     * Variáveis adicionais a serem enviadas ao Camunda.
     *
     * <p>Permite flexibilidade sem alterar a estrutura do objeto.</p>
     */
    private Map<String, Object> variables = new HashMap<>();

    // ===== GETTERS E SETTERS =====

    public MessageClassificationKind getKind() {
        return kind;
    }

    public void setKind(MessageClassificationKind kind) {
        this.kind = kind;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public void addVariable(String key, Object value) {
        this.variables.put(key, value);
    }
}