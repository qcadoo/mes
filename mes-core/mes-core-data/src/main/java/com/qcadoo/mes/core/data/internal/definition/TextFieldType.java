package com.qcadoo.mes.core.data.internal.definition;

import org.apache.commons.lang.StringUtils;

import com.qcadoo.mes.core.data.definition.FieldType;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;

public final class TextFieldType implements FieldType {

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
    public boolean isValidType(final Object value) {
        if (!(value instanceof String)) {
            return false;
        }
        if (StringUtils.length((String) value) > 2048) {
            return false;
        }
        return true;
    }

    @Override
    public int getNumericType() {
        return FieldTypeFactory.NUMERIC_TYPE_TEXT;
    }

}
