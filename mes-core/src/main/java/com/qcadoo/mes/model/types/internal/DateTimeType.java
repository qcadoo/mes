/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.types.internal;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.FieldType;

public final class DateTimeType implements FieldType {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

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
        return Date.class;
    }

    @Override
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        if (value instanceof Date) {
            return value;
        }
        try {
            DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_TIME_FORMAT);
            DateTime dt = fmt.parseDateTime(String.valueOf(value));
            int year = dt.getYear();
            if (year < 1500 || year > 2500) {
                validatedEntity.addError(fieldDefinition, "core.validate.field.error.invalidDateFormat");
                return null;
            }
            return dt.toDate();
        } catch (IllegalArgumentException e) {
            validatedEntity.addError(fieldDefinition, "core.validate.field.error.invalidDateFormat");
        }
        return null;
    }

    @Override
    public String toString(final Object value) {
        return new SimpleDateFormat(DATE_TIME_FORMAT).format((Date) value);
    }

}
