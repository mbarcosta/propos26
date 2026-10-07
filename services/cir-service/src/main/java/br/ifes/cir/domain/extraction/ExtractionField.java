package br.ifes.cir.domain.extraction;

import java.util.ArrayList;
import java.util.List;

public class ExtractionField {
    private String name;
    private String type = "String";
    private boolean required;
    private String label;
    private List<ExtractionField> properties = new ArrayList<>();
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getType() { return type; }
    public void setType(String value) { type = value; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean value) { required = value; }
    public String getLabel() { return label; }
    public void setLabel(String value) { label = value; }
    public List<ExtractionField> getProperties() { return properties; }
    public void setProperties(List<ExtractionField> value) { properties = value == null ? new ArrayList<>() : value; }
}
