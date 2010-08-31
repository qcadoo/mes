package com.qcadoo.mes.core.data.internal.types;

import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class BooleanFieldType implements FieldType {

    @Override
    public boolean isSearchable() {
        return true;
    }

    @Override
    public boolean isOrderable() {
        return true;
    }

    @Override
    public boolean isAggregable() {
        return false;
    }

    @Override
    public int getNumericType() {
        return FieldTypeFactory.NUMERIC_TYPE_BOOLEAN;
    }

    @Override
    public Class<?> getType() {
        return Boolean.class;
    }

    @Override
    public Object fromString(FieldDefinition fieldDefinition, String value, ValidationResults validationResults) {
        return Boolean.parseBoolean(value);
    }

    @Override
    public boolean validate(FieldDefinition fieldDefinition, Object value, ValidationResults validationResults) {
        return true;
    }

}
