package br.ifes.cir.client.dto;

import java.util.Map;

/**
 * Representa o payload enviado ao endpoint /message do Camunda.
 *
 * <p>Este objeto é usado tanto para:</p>
 *
 * <ul>
 *   <li>iniciar novas instâncias por meio de Message Start Event;</li>
 *   <li>correlacionar mensagens intermediárias com instâncias já existentes.</li>
 * </ul>
 *
 * <p>Dependendo do caso de uso, alguns campos podem ou não ser preenchidos:</p>
 *
 * <ul>
 *   <li><b>messageName</b>: sempre obrigatório;</li>
 *   <li><b>businessKey</b>: útil no início de processo, quando já existe
 *       uma chave de negócio para a nova instância;</li>
 *   <li><b>correlationKeys</b>: útil para mensagens intermediárias,
 *       quando a mensagem deve ser entregue a uma instância específica;</li>
 *   <li><b>processVariables</b>: variáveis adicionais entregues ao processo.</li>
 * </ul>
 */
public class MessageCorrelationRequest {

    /**
     * Nome da mensagem BPMN.
     *
     * <p>Exemplos:</p>
     * <ul>
     *   <li>VINCULACAO_START</li>
     *   <li>DEFESA_START</li>
     *   <li>EMAIL_REPLY</li>
     * </ul>
     */
    private String messageName;

    /**
     * Chave de negócio da instância.
     *
     * <p>Usada principalmente em mensagens de início de processo,
     * quando já se deseja criar a instância com um identificador
     * de negócio conhecido.</p>
     */
    private String businessKey;

    /**
     * Chaves de correlação usadas para localizar a instância correta.
     *
     * <p>Usadas principalmente em mensagens intermediárias.
     * Exemplo típico nesta arquitetura:</p>
     *
     * <pre>
     * correlationId = ORI-2026-015
     * </pre>
     */
    private Map<String, VariableValue> correlationKeys;

    /**
     * Variáveis de processo entregues ao Camunda junto com a mensagem.
     *
     * <p>Exemplos típicos:</p>
     * <ul>
     *   <li>subject</li>
     *   <li>body</li>
     *   <li>from</li>
     *   <li>messageId</li>
     *   <li>bindingId</li>
     * </ul>
     */
    private Map<String, VariableValue> processVariables;

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public Map<String, VariableValue> getCorrelationKeys() {
        return correlationKeys;
    }

    public void setCorrelationKeys(Map<String, VariableValue> correlationKeys) {
        this.correlationKeys = correlationKeys;
    }

    public Map<String, VariableValue> getProcessVariables() {
        return processVariables;
    }

    public void setProcessVariables(Map<String, VariableValue> processVariables) {
        this.processVariables = processVariables;
    }
}