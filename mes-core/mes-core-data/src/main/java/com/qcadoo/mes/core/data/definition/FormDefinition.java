package com.qcadoo.mes.core.data.definition;

public class FormDefinition extends ViewElementDefinition {

    public FormDefinition(final String name, final DataDefinition dataDefinition) {
        super(name, dataDefinition);
    }

    @Override
    public int getType() {
        return ViewElementDefinition.TYPE_FORM;
    }

}
