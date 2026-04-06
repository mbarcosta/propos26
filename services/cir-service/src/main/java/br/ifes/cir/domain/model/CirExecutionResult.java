package br.ifes.cir.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado consolidado da execução do CIR.
 *
 * <p>Esse objeto já deixa de ser apenas o retorno bruto do GMS
 * e passa a representar a visão própria do CIR sobre o que foi
 * processado e identificado.</p>
 */
public class CirExecutionResult {

    /**
     * Identificador do binding processado.
     */
    private String bindingId;

    /**
     * Quantidade total de mensagens lidas pelo GMS.
     */
    private int totalRead;

    /**
     * Quantidade de eventos de negócio identificados pelo CIR.
     */
    private int totalEventsIdentified;

    /**
     * Lista de eventos identificados.
     */
    private List<CirIdentifiedEvent> identifiedEvents = new ArrayList<>();

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

    public List<CirIdentifiedEvent> getIdentifiedEvents() {
        return identifiedEvents;
    }

    public void setIdentifiedEvents(List<CirIdentifiedEvent> identifiedEvents) {
        this.identifiedEvents = identifiedEvents;
    }
}