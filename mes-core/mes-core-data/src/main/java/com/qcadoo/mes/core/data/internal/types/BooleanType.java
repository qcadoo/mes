package com.qcadoo.mes.core.data.internal.types;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;

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
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
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
