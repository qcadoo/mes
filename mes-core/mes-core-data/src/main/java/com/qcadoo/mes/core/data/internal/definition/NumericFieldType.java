package com.qcadoo.mes.core.data.internal.definition;

import java.math.BigDecimal;

import com.qcadoo.mes.core.data.definition.FieldType;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;

public final class NumericFieldType implements FieldType {

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
    public boolean isValidType(final Object value) {
        if (!(value instanceof Number)) {
            return false;
        }
        if (scale == 0) {
            if (value instanceof Float) {
                return false;
            }
            if (value instanceof Double) {
                return false;
            }
            if (value instanceof BigDecimal) {
                return false;
            }
        }
        if (((Number) value).longValue() > maxValue) {
            return false;
        }
        return true;
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
