package com.qcadoo.mes.core.data.view.elements;

import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.view.AbstractComponent;
import com.qcadoo.mes.core.data.view.ContainerComponent;
import com.qcadoo.mes.core.data.view.ViewValue;

public class TextInputComponent extends AbstractComponent<String> {

    public TextInputComponent(final String name, final ContainerComponent<?> parent, final String fieldName, final String dataSource) {
        super(name, parent, fieldName, dataSource);
    }

    @Override
    public String getType() {
        return "textInput";
    }

    @Override
    public ViewValue<String> castComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        return new ViewValue<String>(viewObject.getString("value"));
    }

    @Override
    public ViewValue<String> getComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<String> viewEntity, final Set<String> pathsToUpdate) {
        return new ViewValue<String>(getFieldStringValue(entity, selectedEntities));
    }
}
