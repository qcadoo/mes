package com.qcadoo.mes.core.data.internal.types;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;

public final class IntegerType implements FieldType {

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
        return true;
    }

    @Override
    public Class<?> getType() {
        return Integer.class;
    }

    @Override
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        if (value instanceof Integer) {
            return value;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            validatedEntity.addError(fieldDefinition, "commons.validate.field.error.invalidNumericFormat");
            return null;
        }
    }

    @Override
    public String toString(final Object value) {
        return String.valueOf(value);
    }

}
