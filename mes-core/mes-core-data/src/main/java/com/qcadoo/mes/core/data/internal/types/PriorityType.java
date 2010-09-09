package com.qcadoo.mes.core.data.internal.types;

import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class PriorityType implements FieldType {

    private final DataFieldDefinition scopeFieldDefinition;

    public PriorityType(final DataFieldDefinition scopeFieldDefinition) {
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
    public Object toObject(final DataFieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        if (value instanceof Integer) {
            return value;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            validationResults.addError(fieldDefinition, "form.validate.errors.invalidNumericFormat");
            return null;
        }
    }

    @Override
    public String toString(final Object value) {
        return String.valueOf(value);
    }

    public DataFieldDefinition getScopeFieldDefinition() {
        return scopeFieldDefinition;
    }

}
