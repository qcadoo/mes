package com.qcadoo.mes.core.data.definition.form;

import java.util.ArrayList;
import java.util.List;

import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.view.ComponentDefinition;

public final class FormDefinition extends ComponentDefinition {

    private final List<FormFieldDefinition> fields = new ArrayList<FormFieldDefinition>();

    public FormDefinition(final String name, final DataDefinition dataDefinition) {
        super(name, dataDefinition);
    }

    @Override
    public int getType() {
        return ComponentDefinition.TYPE_FORM;
    }

    public List<FormFieldDefinition> getFields() {
        return fields;
    }

    public void addField(final FormFieldDefinition field) {
        fields.add(field);
    }

}
