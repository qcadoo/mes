/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.types.internal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.FieldType;

public final class DateType implements FieldType {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String REPORT_DATE_TIME_FORMAT = "yyyy_MM_dd_HH_mm_ss";

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
            DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_FORMAT);
            DateTime dt = fmt.parseDateTime(String.valueOf(value));

            int year = dt.getYear();
            if (year < 1500 || year > 2500) {
                validatedEntity.addError(fieldDefinition, "core.validate.field.error.invalidDateFormat.range");
                return null;
            }

            Date date = dt.toDate();

            if (year < 2000) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, dt.getYear());
                c.set(Calendar.MONTH, dt.getMonthOfYear() - 1);
                c.set(Calendar.DAY_OF_MONTH, dt.getDayOfMonth());
                c.set(Calendar.HOUR_OF_DAY, dt.hourOfDay().get());
                c.set(Calendar.MINUTE, dt.getMinuteOfHour());
                c.set(Calendar.SECOND, dt.getSecondOfMinute());
                c.set(Calendar.MILLISECOND, dt.getMillisOfSecond());
                date = c.getTime();
            }

            return date;
        } catch (IllegalArgumentException e) {
            validatedEntity.addError(fieldDefinition, "core.validate.field.error.invalidDateFormat");
        }
        return null;
    }

    @Override
    public String toString(final Object value) {
        return new SimpleDateFormat(DATE_FORMAT).format((Date) value);
    }

}
