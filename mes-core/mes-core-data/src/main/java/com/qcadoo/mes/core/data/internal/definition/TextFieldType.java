package com.qcadoo.mes.core.data.internal.definition;

import com.qcadoo.mes.core.data.definition.FieldType;

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
        return value instanceof String;
    }

}
