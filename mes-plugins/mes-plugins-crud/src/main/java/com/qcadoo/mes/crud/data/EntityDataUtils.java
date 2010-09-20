package com.qcadoo.mes.crud.data;

import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.ModelDefinition;

public class EntityDataUtils {

    private EntityDataUtils() {

    }

    public static Entity generateEntityData(final Entity entity, final ModelDefinition dataDefinition) {
        Entity stringEntity = new Entity(entity.getId());
        for (Map.Entry<String, Object> entry : entity.getFields().entrySet()) {
            stringEntity.setField(entry.getKey(), dataDefinition.getField(entry.getKey()).getValue(entry.getValue()));
        }
        return stringEntity;
    }

}
