package br.ifes.cir.client.dto;

import java.util.Map;

/**
 * Representa o payload enviado para iniciar um processo no Camunda.
 */
public class StartProcessRequest {

    private Map<String, VariableValue> variables;

    public Map<String, VariableValue> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, VariableValue> variables) {
        this.variables = variables;
    }
}