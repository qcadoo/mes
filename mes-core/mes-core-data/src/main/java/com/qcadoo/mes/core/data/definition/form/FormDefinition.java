package com.qcadoo.mes.core.data.definition.form;

import java.util.LinkedHashMap;
import java.util.Map;

import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.view.ComponentDefinition;

public final class FormDefinition extends ComponentDefinition {

    private final Map<String, FormFieldDefinition> fields = new LinkedHashMap<String, FormFieldDefinition>();

    public FormDefinition(final String name, final DataDefinition dataDefinition) {
        super(name, dataDefinition);
    }

    @Override
    public int getType() {
        return ComponentDefinition.TYPE_FORM;
    }

    public Map<String, FormFieldDefinition> getFields() {
        return fields;
    }

    public void addField(final FormFieldDefinition field) {
        fields.put(field.getName(), field);
    }

    public FormFieldDefinition getField(final String fieldName) {
        return fields.get(fieldName);
    }

}
