package com.qcadoo.mes.core.data.internal.types;

import java.math.BigDecimal;

import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.ValidationResults;

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
    public int getNumericType() {
        if (scale == 0) {
            return FieldTypeFactory.NUMERIC_TYPE_INTEGER;
        } else {
            return FieldTypeFactory.NUMERIC_TYPE_DECIMAL;
        }
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
    public Object fromString(final DataFieldDefinition fieldDefinition, final String value, final ValidationResults validationResults) {
        try {
            if (scale == 0) {
                return Integer.parseInt(value);
            } else {
                return new BigDecimal(value);
            }
        } catch (NumberFormatException e) {
            validationResults.addError(fieldDefinition, "form.validate.errors.invalidNumericFormat");
            return null;
        }
    }

    @Override
    public String toString(final Object value) {
        return String.valueOf(value);
    }

    @Override
    public boolean validate(final DataFieldDefinition fieldDefinition, final Object value, final ValidationResults validationResults) {
        if (((Number) value).longValue() > maxValue) {
            validationResults.addError(fieldDefinition, "form.validate.errors.numericIsTooBig", String.valueOf(maxValue));
            return false;
        }
        return true;
    }

}
