package com.qcadoo.mes.core.data.internal.types;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class DateTimeFieldType implements FieldType {

    private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

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
    public int getNumericType() {
        return FieldTypeFactory.NUMERIC_TYPE_DATE_TIME;
    }

    @Override
    public Class<?> getType() {
        return Date.class;
    }

    @Override
    public Object fromString(FieldDefinition fieldDefinition, String value, ValidationResults validationResults) {
        try {
            return FORMAT.parse((String) value);
        } catch (ParseException e) {
            validationResults.addError(fieldDefinition, "form.validate.errors.invalidDateTimeFormat");
        }
        return null;
    }

    @Override
    public boolean validate(FieldDefinition fieldDefinition, Object value, ValidationResults validationResults) {
        return true;
    }

}
