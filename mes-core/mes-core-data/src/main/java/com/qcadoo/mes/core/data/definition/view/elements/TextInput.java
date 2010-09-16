package com.qcadoo.mes.core.data.definition.view.elements;

import java.util.Map;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.view.ComponentDefinition;

public class TextInput extends ComponentDefinition {

    public TextInput(final String name, final String dataSource) {
        super(name, dataSource);
    }

    @Override
    public int getType() {
        return ComponentDefinition.TYPE_ELEMENT_TEXT_INPUT;
    }

    @Override
    public String getValue(DataDefinition dataDefinition, DataAccessService dataAccessService, Entity entity) {
        Object value = entity.getField(getDataSource());
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }

    @Override
    public Object getUpdateValues(Map<String, String> updateComponents) {
        return null;
    }
}
