package com.qcadoo.mes.core.data.definition.view.elements;

import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.view.ComponentDefinition;
import com.qcadoo.mes.core.data.definition.view.ContainerDefinition;

public class TextInput extends ComponentDefinition {

    public TextInput(final String name, final ContainerDefinition parent, final String fieldName, final String dataSource) {
        super(name, parent, fieldName, dataSource);
    }

    @Override
    public String getType() {
        return "textInput";
    }

    @Override
    public Object getValue(final Entity entity, final Map<String, Object> selectableValues, final Object viewEntity) {
        return getFieldValue(entity);
    }

}
