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
    public Object fromString(final FieldDefinition fieldDefinition, final String value, final ValidationResults validationResults) {
        return Boolean.parseBoolean(value);
    }

    @Override
    public String toString(final Object value) {
        if ((Boolean) value) {
            return "1";
        } else {
            return "0";
        }
    }

    @Override
    public boolean validate(final FieldDefinition fieldDefinition, final Object value, final ValidationResults validationResults) {
        return true;
    }

}
