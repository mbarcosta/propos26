package br.ifes.email_gateway_service.model;

import java.util.List;

/**
 * Representa o arquivo de configuração de bindings de e-mail.
 *
 * Essa classe funciona como objeto raiz para desserialização do arquivo JSON
 * que contém a lista de bindings disponíveis no sistema.
 *
 * Estrutura esperada no JSON:
 *
 * {
 *   "bindings": [
 *     { ... },
 *     { ... }
 *   ]
 * }
 */
public class MailBindingConfig {

    /**
     * Lista de bindings cadastrados no sistema.
     */
    private List<MailBinding> bindings;

    public MailBindingConfig() {
    }

    public List<MailBinding> getBindings() {
        return bindings;
    }

    public void setBindings(List<MailBinding> bindings) {
        this.bindings = bindings;
    }
}