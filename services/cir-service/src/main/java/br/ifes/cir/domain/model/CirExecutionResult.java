package br.ifes.cir.domain.model;

import java.util.ArrayList;
import java.util.List;

import br.ifes.cir.domain.rule.ClassifiedMessage;

/**
 * Resultado consolidado da execução do CIR.
 *
 * <p>Este objeto representa a visão própria do CIR sobre um ciclo
 * de processamento de mensagens de uma caixa postal monitorada.</p>
 *
 * <p>Nesta nova fase da arquitetura, o CIR deixa de devolver
 * eventos específicos de processo, como por exemplo
 * "VINCULACAO_RECEBIDA", e passa a devolver mensagens classificadas
 * de forma genérica.</p>
 *
 * <p>Com isso, o resultado da execução passa a refletir melhor
 * o novo papel do CIR:</p>
 *
 * <ul>
 *   <li>identificar mensagens de início de processo;</li>
 *   <li>identificar mensagens intermediárias correlacionáveis;</li>
 *   <li>ignorar mensagens irrelevantes.</li>
 * </ul>
 */
public class CirExecutionResult {

    /**
     * Identificador do binding processado.
     */
    private String bindingId;

    /**
     * Quantidade total de mensagens lidas pelo GMS
     * durante a execução.
     */
    private int totalRead;

    /**
     * Quantidade total de mensagens classificadas pelo CIR
     * como relevantes para encaminhamento ao Camunda.
     *
     * <p>Em outras palavras, representa a quantidade de mensagens
     * classificadas como START ou REPLY.</p>
     */
    private int totalEventsIdentified;

    /**
     * Lista de mensagens classificadas pelo CIR.
     *
     * <p>Esta lista contém apenas mensagens relevantes ao fluxo,
     * já interpretadas na forma de {@link ClassifiedMessage}.</p>
     */
    private List<ClassifiedMessage> identifiedEvents = new ArrayList<>();

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public int getTotalRead() {
        return totalRead;
    }

    public void setTotalRead(int totalRead) {
        this.totalRead = totalRead;
    }

    public int getTotalEventsIdentified() {
        return totalEventsIdentified;
    }

    public void setTotalEventsIdentified(int totalEventsIdentified) {
        this.totalEventsIdentified = totalEventsIdentified;
    }

    public List<ClassifiedMessage> getIdentifiedEvents() {
        return identifiedEvents;
    }

    public void setIdentifiedEvents(List<ClassifiedMessage> identifiedEvents) {
        this.identifiedEvents = identifiedEvents;
    }
}