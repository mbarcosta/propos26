package br.ifes.email_gateway_service.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa o resultado consolidado de uma operação de polling
 * executada sobre um binding específico.
 *
 * Nesta nova versão, o GMS apenas lê mensagens da caixa postal
 * e as devolve de forma padronizada.
 */
public class PollResult {

    private String bindingId;
    private int totalRead;
    private List<PollItemResult> results = new ArrayList<>();
    private List<EmailMessage> messages = new ArrayList<>();

    public PollResult() {
    }

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

    public List<PollItemResult> getResults() {
        return results;
    }

    public void setResults(List<PollItemResult> results) {
        this.results = results;
    }

    public List<EmailMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<EmailMessage> messages) {
        this.messages = messages;
    }
}