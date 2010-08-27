package com.qcadoo.mes.core.data.internal.definition;

import java.util.Arrays;
import java.util.List;

import com.qcadoo.mes.core.data.definition.EnumeratedFieldType;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;
import com.qcadoo.mes.core.data.internal.ValidatableFieldType;

public final class EnumFieldType implements EnumeratedFieldType, ValidatableFieldType {

    private final List<String> values;

    public EnumFieldType(final String... values) {
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
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public String validateValue(final Object value) {
        if (!values.contains(value)) {
            return String.valueOf(value) + " must be one the " + values;
        }
        return null;
    }

    @Override
    public List<String> values() {
        return values;
    }

    @Override
    public int getNumericType() {
        return FieldTypeFactory.NUMERIC_TYPE_ENUM;
    }

}
