/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.types.internal;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.FieldType;

public final class BooleanType implements FieldType {

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
    public Class<?> getType() {
        return Boolean.class;
    }

    @Override
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        if (value instanceof Boolean) {
            return value;
        }
        return parseStringToBoolean(String.valueOf(value));
    }

    private Boolean parseStringToBoolean(final String value) {
        return "1".equals(value) || "true".equals(value) || "yes".equals(value);
    }

    private String parseBooleanToString(final Boolean value) {
        if (value) {
            return "1";
        } else {
            return "0";
        }
    }

    @Override
    public String toString(final Object value) {
        if (value instanceof Boolean) {
            return parseBooleanToString((Boolean) value);
        } else {
            return parseBooleanToString(parseStringToBoolean(String.valueOf(value)));
        }
    }

}