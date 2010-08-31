package com.qcadoo.mes.core.data.definition;

public class FormDefinition extends ViewElementDefinition {

    public FormDefinition(String name, DataDefinition dataDefinition) {
        super(name, dataDefinition);
    }

    public int getType() {
        return ViewElementDefinition.TYPE_FORM;
    }

}
