package com.qcadoo.mes.core.data.internal.types;

import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class BooleanType implements FieldType {

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
    public Class<?> getType() {
        return Boolean.class;
    }

    @Override
    public Object toObject(final DataFieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        if (value instanceof Boolean) {
            return value;
        }
        return "1".equals(String.valueOf(value));
    }

    @Override
    public String toString(final Object value) {
        if ((Boolean) value) {
            return "1";
        } else {
            return "0";
        }
    }

}
