package br.ifes.cir.domain.model;

/**
 * Representa a decisão tomada pelo CIR sobre como um evento
 * identificado deve ser tratado no motor de processos.
 *
 * <p>Essa classe encapsula:</p>
 * <ul>
 *   <li>o tipo de ação (start ou correlate)</li>
 *   <li>o alvo da ação (nome do processo ou nome da mensagem BPMN)</li>
 * </ul>
 *
 * <p>Exemplos:</p>
 * <ul>
 *   <li>START_PROCESS + "processo_vinculacao"</li>
 *   <li>CORRELATE_MESSAGE + "MensagemVinculacaoRecebida"</li>
 * </ul>
 *
 * <p>Essa estrutura permite separar claramente:</p>
 * <ul>
 *   <li>a lógica de decisão (domínio)</li>
 *   <li>da execução técnica (CamundaClient)</li>
 * </ul>
 */
public class ProcessDispatchDecision {

    /**
     * Tipo de ação a ser executada no motor de processo.
     */
    private ProcessActionType actionType;

    /**
     * Nome do alvo da ação.
     *
     * <p>Pode representar:</p>
     * <ul>
     *   <li>processDefinitionKey (quando START_PROCESS)</li>
     *   <li>messageName (quando CORRELATE_MESSAGE)</li>
     * </ul>
     */
    private String targetName;

    /**
     * Construtor padrão.
     */
    public ProcessDispatchDecision() {
    }

    /**
     * Construtor completo.
     *
     * @param actionType tipo da ação
     * @param targetName nome do processo ou da mensagem
     */
    public ProcessDispatchDecision(ProcessActionType actionType, String targetName) {
        this.actionType = actionType;
        this.targetName = targetName;
    }

    public ProcessActionType getActionType() {
        return actionType;
    }

    public void setActionType(ProcessActionType actionType) {
        this.actionType = actionType;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
}