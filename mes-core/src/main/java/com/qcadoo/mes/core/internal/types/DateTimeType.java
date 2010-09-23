package com.qcadoo.mes.core.internal.types;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.model.FieldDefinition;
import com.qcadoo.mes.core.types.FieldType;

public final class DateTimeType implements FieldType {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

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
            return new SimpleDateFormat(DATE_TIME_FORMAT).parse(String.valueOf(value));
        } catch (ParseException e) {
            validatedEntity.addError(fieldDefinition, "commons.validate.field.error.invalidDateTimeFormat");
        }
        return null;
    }

    @Override
    public String toString(final Object value) {
        return new SimpleDateFormat(DATE_TIME_FORMAT).format((Date) value);
    }

}
