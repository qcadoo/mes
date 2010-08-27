package com.qcadoo.mes.core.data.internal.definition;

import java.math.BigDecimal;

import com.qcadoo.mes.core.data.definition.FieldType;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;
import com.qcadoo.mes.core.data.internal.ValidatableFieldType;

public final class NumericFieldType implements FieldType, ValidatableFieldType {

    private final int scale;

    private final int precision;

    private final long maxValue;

    public NumericFieldType(final int precision, final int scale) {
        this.scale = scale;
        this.precision = precision;
        this.maxValue = (long) Math.pow(10, precision - scale) - 1;
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
        return true;
    }

    @Override
    public Class<?> getType() {
        if (scale == 0) {
            return Integer.class;
        } else {
            return BigDecimal.class;
        }
    }

    @Override
    public String validateValue(final Object value) {
        if (((Number) value).longValue() > maxValue) {
            return "value is too big, " + value + " > " + maxValue;
        }
        return null;
    }

    @Override
    public int getNumericType() {
        if (scale == 0) {
            return FieldTypeFactory.NUMERIC_TYPE_INTEGER;
        } else {
            return FieldTypeFactory.NUMERIC_TYPE_DECIMAL;
        }
    }

}
