package com.qcadoo.mes.core.data.internal.types;

import java.util.Arrays;
import java.util.List;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.types.EnumeratedFieldType;

public final class EnumType implements EnumeratedFieldType {

    private final List<String> values;

    public EnumType(final String... values) {
        this.values = Arrays.asList(values);
    }

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
    public List<String> values() {
        return values;
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        String stringValue = String.valueOf(value);
        if (!values().contains(stringValue)) {
            validatedEntity.addError(fieldDefinition, "commons.validate.field.error.invalidDictionaryItem",
                    String.valueOf(values()));
            return null;
        }
        return stringValue;
    }

    @Override
    public String toString(final Object value) {
        return String.valueOf(value);
    }

}
