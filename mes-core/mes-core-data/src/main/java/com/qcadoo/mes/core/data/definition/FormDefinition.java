package com.qcadoo.mes.core.data.definition;

import java.util.LinkedList;
import java.util.List;

public class FormDefinition extends ViewElementDefinition {

    private List<FieldViewDefinition> fields = new LinkedList<FieldViewDefinition>();

    public FormDefinition(final String name, final DataDefinition dataDefinition) {
        super(name, dataDefinition);
    }

    @Override
    public int getType() {
        return ViewElementDefinition.TYPE_FORM;
    }

    public List<FieldViewDefinition> getFields() {
        return fields;
    }

    public void setFields(List<FieldViewDefinition> fields) {
        this.fields = fields;
    }

}
