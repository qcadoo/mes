package com.qcadoo.mes.core.data.definition;

public class FieldViewDefinition {

    private String label;

    private FieldDefinition definition;

    public FieldViewDefinition(String label, FieldDefinition definition) {
        super();
        this.label = label;
        this.definition = definition;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public FieldDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(FieldDefinition definition) {
        this.definition = definition;
    }

}
