/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.types.internal;

import java.math.BigDecimal;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.FieldType;

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
                validatedEntity.addError(fieldDefinition, "core.validate.field.error.invalidNumericFormat");
                return null;
            }
        }
        if (decimal.precision() > 7 || decimal.scale() > 3) {
            validatedEntity.addError(fieldDefinition, "core.validate.field.error.invalidNumericFormat");
            return null;
        }
        return decimal;
    }

    @Override
    public String toString(final Object value) {
        return String.valueOf(value);
    }

}
