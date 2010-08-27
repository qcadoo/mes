package com.qcadoo.mes.core.data.internal.definition;

import org.apache.commons.lang.StringUtils;

import com.qcadoo.mes.core.data.definition.FieldType;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;
import com.qcadoo.mes.core.data.internal.ValidatableFieldType;

public final class StringFieldType implements FieldType, ValidatableFieldType {

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
        if (StringUtils.length((String) value) > 255) {
            return "value is too long, " + StringUtils.length((String) value) + " > 255";
        }
        return null;
    }

    @Override
    public int getNumericType() {
        return FieldTypeFactory.NUMERIC_TYPE_STRING;
    }

}
