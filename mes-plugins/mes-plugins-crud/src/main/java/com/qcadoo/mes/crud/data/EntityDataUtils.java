package com.qcadoo.mes.crud.data;

import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public class EntityDataUtils {

    private EntityDataUtils() {

    }

    public static Entity generateEntityData(final Entity entity, final DataDefinition dataDefinition) {
        Entity stringEntity = new Entity(entity.getId());
        for (Map.Entry<String, Object> entry : entity.getFields().entrySet()) {
            stringEntity.setField(entry.getKey(), dataDefinition.getField(entry.getKey()).getValue(entry.getValue()));
        }
        return stringEntity;
    }

    public static ValidationResults generateValidationResultWithEntityData(final ValidationResults validationResults,
            final DataDefinition dataDefinition) {
        if (validationResults.getEntity() != null) {
            validationResults.setEntity(generateEntityData(validationResults.getEntity(), dataDefinition));
        }
        return validationResults;
    }

}
