package br.ifes.email_gateway_service.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa o resultado consolidado de uma operação de polling
 * executada sobre um binding específico.
 */
public class PollResult {

    private String bindingId;
    private int processedCount;
    private int ignoredCount;
    private int errorCount;
    private List<PollItemResult> results = new ArrayList<>();
    private List<EmailEvent> events = new ArrayList<>();

    public PollResult() {
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(int processedCount) {
        this.processedCount = processedCount;
    }

    public int getIgnoredCount() {
        return ignoredCount;
    }

    public void setIgnoredCount(int ignoredCount) {
        this.ignoredCount = ignoredCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<PollItemResult> getResults() {
        return results;
    }

    public void setResults(List<PollItemResult> results) {
        this.results = results;
    }

    public List<EmailEvent> getEvents() {
        return events;
    }

    public void setEvents(List<EmailEvent> events) {
        this.events = events;
    }
}