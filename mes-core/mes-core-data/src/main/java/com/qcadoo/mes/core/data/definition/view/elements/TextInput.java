package com.qcadoo.mes.core.data.definition.view.elements;

import java.util.Map;

import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.view.ComponentDefinition;
import com.qcadoo.mes.core.data.definition.view.ContainerDefinition;
import com.qcadoo.mes.core.data.definition.view.ViewEntity;

public class TextInput extends ComponentDefinition<String> {

    public TextInput(final String name, final ContainerDefinition<?> parent, final String fieldName, final String dataSource) {
        super(name, parent, fieldName, dataSource);
    }

    @Override
    public String getType() {
        return "textInput";
    }

    @Override
    public ViewEntity<String> castValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewEntity) {
        System.out.println(viewEntity.toString());
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ViewEntity<String> getComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewEntity<Object> globalViewEntity, final ViewEntity<String> viewEntity) {
        return new ViewEntity<String>(getFieldValue(entity, getFieldPath()));
    }
}
