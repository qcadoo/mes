package com.qcadoo.mes.core.data.internal.types;

import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class PriorityFieldType implements FieldType {

    private final FieldDefinition scopeFieldDefinition;

    public PriorityFieldType(final FieldDefinition scopeFieldDefinition) {
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
    public int getNumericType() {
        return FieldTypeFactory.NUMERIC_TYPE_PRIORITY;
    }

    @Override
    public Class<?> getType() {
        return Integer.class;
    }

    @Override
    public Object fromString(final FieldDefinition fieldDefinition, final String value, final ValidationResults validationResults) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            validationResults.addError(fieldDefinition, "form.validate.errors.invalidNumericFormat");
            return null;
        }
    }

    @Override
    public String toString(final Object value) {
        return String.valueOf(value);
    }

    @Override
    public boolean validate(final FieldDefinition fieldDefinition, final Object value, final ValidationResults validationResults) {
        return true;
    }

    public FieldDefinition getScopeFieldDefinition() {
        return scopeFieldDefinition;
    }

}
