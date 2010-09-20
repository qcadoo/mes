package com.qcadoo.mes.core.data.view.elements;

import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.view.ComponentDefinitionImpl;
import com.qcadoo.mes.core.data.view.ContainerComponent;
import com.qcadoo.mes.core.data.view.ViewEntity;

public class TextInput extends ComponentDefinitionImpl<String> {

    public TextInput(final String name, final ContainerComponent<?> parent, final String fieldName, final String dataSource) {
        super(name, parent, fieldName, dataSource);
    }

    @Override
    public String getType() {
        return "textInput";
    }

    @Override
    public ViewEntity<String> castComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        return new ViewEntity<String>(viewObject.getString("value"));
    }

    @Override
    public ViewEntity<String> getComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewEntity<String> viewEntity, final Set<String> pathsToUpdate) {
        return new ViewEntity<String>(getFieldStringValue(entity, selectedEntities));
    }
}
