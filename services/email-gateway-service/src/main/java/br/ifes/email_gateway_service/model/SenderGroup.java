package br.ifes.email_gateway_service.model;

import java.util.List;

/**
 * Representa um grupo reutilizável de remetentes autorizados.
 */
public class SenderGroup {

    private String name;
    private SenderGroupType type;
    private List<String> senders;
    private List<String> domains;

    public SenderGroup() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SenderGroupType getType() {
        return type;
    }

    public void setType(SenderGroupType type) {
        this.type = type;
    }

    public List<String> getSenders() {
        return senders;
    }

    public void setSenders(List<String> senders) {
        this.senders = senders;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }
}