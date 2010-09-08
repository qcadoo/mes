package com.qcadoo.mes.core.data.internal.types;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class DateType implements FieldType {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    private static final String DATE_FORMAT_ERROR_MESSAGE = "form.validate.errors.invalidDateFormat";

    private static final String DATE_TIME_FORMAT_ERROR_MESSAGE = "form.validate.errors.invalidDateTimeFormat";

    private final String format;

    private final String errorMessage;

    private final boolean withTime;

    public DateType(final boolean withTime) {
        this.withTime = withTime;
        if (withTime) {
            format = DATE_TIME_FORMAT;
            errorMessage = DATE_FORMAT_ERROR_MESSAGE;
        } else {
            format = DATE_FORMAT;
            errorMessage = DATE_TIME_FORMAT_ERROR_MESSAGE;
        }
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
        return false;
    }

    @Override
    public Class<?> getType() {
        return Date.class;
    }

    @Override
    public Object fromString(final DataFieldDefinition fieldDefinition, final String value,
            final ValidationResults validationResults) {
        try {
            return new SimpleDateFormat(format).parse(value);
        } catch (ParseException e) {
            validationResults.addError(fieldDefinition, errorMessage);
        }
        return null;
    }

    @Override
    public String toString(final Object value) {
        return new SimpleDateFormat(format).format((Date) value);
    }

    @Override
    public boolean validate(final DataFieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        return true;
    }

}
