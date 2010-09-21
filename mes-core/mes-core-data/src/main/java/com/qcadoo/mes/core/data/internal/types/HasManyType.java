package com.qcadoo.mes.core.data.internal.types;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.ModelDefinition;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.view.elements.grid.ListData;

public class HasManyType implements FieldType {

    private final String entityName;

    private final String fieldName;

    private final DataDefinitionService dataDefinitionService;

    public HasManyType(final String entityName, final String fieldName, final DataDefinitionService dataDefinitionService) {
        this.entityName = entityName;
        this.fieldName = fieldName;
        this.dataDefinitionService = dataDefinitionService;
    }

    @Override
    public boolean isSearchable() {
        return false;
    }

    @Override
    public boolean isOrderable() {
        return false;
    }

    @Override
    public boolean isAggregable() {
        return false;
    }

    @Override
    public Class<?> getType() {
        return ListData.class;
    }

    @Override
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        return null;
    }

    @Override
    public String toString(final Object value) {
        return null;
    }

    public String getFieldName() {
        return fieldName;
    }

    public ModelDefinition getDataDefinition() {
        return dataDefinitionService.get(entityName);
    }

}
