package br.ifes.cir.client.dto;

import java.util.List;

/**
 * DTO que representa o resultado retornado pelo GMS
 * após a execução do polling de um binding.
 *
 * <p>Este objeto espelha a estrutura JSON atualmente devolvida
 * pelo endpoint do GMS.</p>
 */
public class GmsPollResult {

    /**
     * Identificador do binding processado.
     */
    private String bindingId;

    /**
     * Quantidade total de mensagens lidas pelo GMS.
     */
    private int totalRead;

    /**
     * Lista resumida com o resultado operacional da leitura
     * de cada e-mail processado.
     */
    private List<GmsReadResult> results;

    /**
     * Lista detalhada das mensagens efetivamente lidas.
     */
    private List<GmsMessage> messages;

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

    public List<GmsReadResult> getResults() {
        return results;
    }

    public void setResults(List<GmsReadResult> results) {
        this.results = results;
    }

    public List<GmsMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<GmsMessage> messages) {
        this.messages = messages;
    }
}