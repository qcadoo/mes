package com.qcadoo.mes.core.data.internal.types;

import java.math.BigDecimal;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;

public final class DecimalType implements FieldType {

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
        return BigDecimal.class;
    }

    @Override
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        BigDecimal decimal = null;

        if (value instanceof BigDecimal) {
            decimal = (BigDecimal) value;
        } else {
            try {
                decimal = new BigDecimal(String.valueOf(value));
            } catch (NumberFormatException e) {
                validatedEntity.addError(fieldDefinition, "commons.validate.field.error.invalidNumericFormat");
                return null;
            }
        }
        if (decimal.precision() > 7 || decimal.scale() > 3) {
            validatedEntity.addError(fieldDefinition, "commons.validate.field.error.invalidNumericFormat");
            return null;
        }
        return decimal;
    }

    @Override
    public String toString(final Object value) {
        return String.valueOf(value);
    }

}
