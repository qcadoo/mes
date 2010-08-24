package com.qcadoo.mes.core.data.internal.definition;

import java.util.Date;

import com.qcadoo.mes.core.data.definition.FieldType;

public final class DateTimeFieldType implements FieldType {

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
        return value instanceof Date;
    }

    @Override
    public int getNumericType() {
        return NUMERIC_TYPE_DATE_TIME;
    }
}
