package com.qcadoo.mes.core.data.internal.definition;

import org.apache.commons.lang.StringUtils;

import com.qcadoo.mes.core.data.definition.FieldType;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;
import com.qcadoo.mes.core.data.internal.ValidatableFieldType;

public final class TextFieldType implements FieldType, ValidatableFieldType {

    @Override
    public boolean isSearchable() {
        return false;
    }

    @Override
    public boolean isOrderable() {
        return false;
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
        if (StringUtils.length((String) value) > 2048) {
            return "value is too long, " + StringUtils.length((String) value) + " > 2048";
        }
        return null;
    }

    @Override
    public int getNumericType() {
        return FieldTypeFactory.NUMERIC_TYPE_TEXT;
    }

}
