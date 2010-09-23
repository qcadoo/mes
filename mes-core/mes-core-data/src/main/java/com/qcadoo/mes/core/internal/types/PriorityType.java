package com.qcadoo.mes.core.internal.types;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.model.FieldDefinition;
import com.qcadoo.mes.core.types.FieldType;

public final class PriorityType implements FieldType {

    private final FieldDefinition scopeFieldDefinition;

    public PriorityType(final FieldDefinition scopeFieldDefinition) {
        this.scopeFieldDefinition = scopeFieldDefinition;
    }

    @Override
    public boolean isSearchable() {
        return false;
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
            validatedEntity.addError(fieldDefinition, "form.validate.errors.invalidNumericFormat");
            return null;
        }
    }

    @Override
    public String toString(final Object value) {
        return String.valueOf(value);
    }

    public FieldDefinition getScopeFieldDefinition() {
        return scopeFieldDefinition;
    }

}
