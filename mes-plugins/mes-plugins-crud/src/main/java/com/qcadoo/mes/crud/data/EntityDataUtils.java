package com.qcadoo.mes.crud.data;

import java.util.Map;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.model.DataDefinition;

public final class EntityDataUtils {

    private EntityDataUtils() {
    }

    public static Entity generateEntityData(final Entity entity, final DataDefinition dataDefinition) {
        Entity stringEntity = entity.copy();
        for (Map.Entry<String, Object> entry : entity.getFields().entrySet()) {
            stringEntity.setField(entry.getKey(), dataDefinition.getField(entry.getKey()).getValue(entry.getValue()));
        }
        return stringEntity;
    }

}
