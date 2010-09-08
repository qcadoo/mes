package com.qcadoo.mes.core.data.definition;

public class FieldViewDefinition {

    private FieldDefinition definition;

    private boolean readOnly = false;

    public FieldViewDefinition(FieldDefinition definition) {
        super();
        this.definition = definition;
    }

    public FieldDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(FieldDefinition definition) {
        this.definition = definition;
    }

    public boolean isReadOnly() {
        return readOnly || definition.isReadOnly();
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

}
