package com.qcadoo.mes.core.data.internal.definition;

import com.qcadoo.mes.core.data.definition.FieldType;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;
import com.qcadoo.mes.core.data.internal.ValidatableFieldType;

public final class BooleanFieldType implements FieldType, ValidatableFieldType {

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
        return FieldTypeFactory.NUMERIC_TYPE_BOOLEAN;
    }

    @Override
    public Class<?> getType() {
        return Boolean.class;
    }

    @Override
    public String validateValue(final Object value) {
        return null;
    }

}
