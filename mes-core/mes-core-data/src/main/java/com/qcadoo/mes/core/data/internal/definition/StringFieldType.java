package com.qcadoo.mes.core.data.internal.definition;

import org.apache.commons.lang.StringUtils;

import com.qcadoo.mes.core.data.definition.FieldType;

public final class StringFieldType implements FieldType {

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
        if (StringUtils.length((String) value) > 255) {
            return false;
        }
        return true;
    }

}
