package com.qcadoo.mes.core.data.internal.definition;

import java.util.Date;

import com.qcadoo.mes.core.data.definition.FieldType;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;
import com.qcadoo.mes.core.data.internal.ValidatableFieldType;

public final class DateTimeFieldType implements FieldType, ValidatableFieldType {

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
    public int getNumericType() {
        return FieldTypeFactory.NUMERIC_TYPE_DATE_TIME;
    }

    @Override
    public Class<?> getType() {
        return Date.class;
    }

    @Override
    public String validateValue(final Object value) {
        return null;
    }

}
