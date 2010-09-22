package com.qcadoo.mes.core.data.internal.validators;

import java.math.BigDecimal;
import java.util.Date;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.DataDefinition;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.validation.FieldValidator;

public final class RangeValidator implements FieldValidator {

    private static final String OUT_OF_RANGE_ERROR = "commons.validate.field.error.outOfRange";

    private String errorMessage = OUT_OF_RANGE_ERROR;

    private final Object from;

    private final Object to;

    public RangeValidator(final Object from, final Object to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final Entity validatedEntity) {
        if (value == null) {
            return true;
        }

        Class<?> fieldClass = fieldDefinition.getType().getType();

        if (fieldClass.equals(String.class)) {
            return validateStringRange(fieldDefinition, (String) value, validatedEntity);
        } else if (fieldClass.equals(Integer.class) || fieldClass.equals(BigDecimal.class)) {
            return validateNumberRange(fieldDefinition, (Number) value, validatedEntity);
        } else if (fieldClass.equals(Date.class)) {
            return validateDateRange(fieldDefinition, (Date) value, validatedEntity);
        }

        return true;
    }

    private boolean validateDateRange(final FieldDefinition fieldDefinition, final Date value, final Entity validatedEntity) {
        if (from != null && value.before((Date) from)) {
            addError(fieldDefinition, validatedEntity);
            return false;
        }
        if (to != null && value.after((Date) to)) {
            addError(fieldDefinition, validatedEntity);
            return false;
        }
        return true;
    }

    private boolean validateNumberRange(final FieldDefinition fieldDefinition, final Number value, final Entity validatedEntity) {
        if (from != null && value.doubleValue() < ((Number) from).doubleValue()) {
            addError(fieldDefinition, validatedEntity);
            return false;
        }
        if (to != null && value.doubleValue() > ((Number) to).doubleValue()) {
            addError(fieldDefinition, validatedEntity);
            return false;
        }
        return true;
    }

    private boolean validateStringRange(final FieldDefinition fieldDefinition, final String value, final Entity validatedEntity) {
        if (from != null && value.compareTo((String) from) < 0) {
            addError(fieldDefinition, validatedEntity);
            return false;
        }
        if (to != null && value.compareTo((String) to) > 0) {
            addError(fieldDefinition, validatedEntity);
            return false;
        }
        return true;
    }

    private void addError(final FieldDefinition fieldDefinition, final Entity validatedEntity) {
        validatedEntity.addError(fieldDefinition, errorMessage, String.valueOf(from), String.valueOf(to));
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Entity entity) {
        return true;
    }

    @Override
    public FieldValidator customErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }
}
