package br.ifes.cir.client.dto;

/**
 * Representa uma variável no formato esperado pelo Camunda REST API.
 */
public class VariableValue {

    private Object value;
    private String type;

    public VariableValue() {
    }

    public VariableValue(Object value, String type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}