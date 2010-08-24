package com.qcadoo.mes.core.data.internal.definition;

import java.util.Arrays;
import java.util.List;

import com.qcadoo.mes.core.data.definition.EnumeratedFieldType;

public final class EnumFieldType implements EnumeratedFieldType {

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
    public boolean isValidType(final Object value) {
        if (!(value instanceof String)) {
            return false;
        }
        if (!values.contains(value)) {
            return false;
        }
        return true;
    }

    @Override
    public List<String> values() {
        return values;
    }

    @Override
    public int getNumericType() {
        return NUMERIC_TYPE_ENUM;
    }

}
