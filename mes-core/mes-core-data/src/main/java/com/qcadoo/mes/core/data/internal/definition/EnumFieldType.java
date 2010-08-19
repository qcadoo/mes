package com.qcadoo.mes.core.data.internal.definition;

import java.util.Arrays;
import java.util.List;

import com.qcadoo.mes.core.data.definition.FieldType;

public class EnumFieldType implements FieldType {

    private final List<String> values;

    public EnumFieldType(String... values) {
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
    public boolean isValidType(Object value) {
        if (!(value instanceof String)) {
            return false;
        }
        if (!values.contains(value)) {
            return false;
        }
        return true;
    }
}
