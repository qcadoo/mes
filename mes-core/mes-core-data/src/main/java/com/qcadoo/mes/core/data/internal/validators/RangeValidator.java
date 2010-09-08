package com.qcadoo.mes.core.data.internal.validators;

import java.math.BigDecimal;
import java.util.Date;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

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
    public boolean validate(final DataDefinition dataDefinition, final DataFieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        if (value == null) {
            return true;
        }

        Class<?> fieldClass = fieldDefinition.getType().getType();

        if (fieldClass.equals(String.class)) {
            return validateStringRange(fieldDefinition, (String) value, validationResults);
        } else if (fieldClass.equals(Integer.class) || fieldClass.equals(BigDecimal.class)) {
            return validateNumberRange(fieldDefinition, (Number) value, validationResults);
        } else if (fieldClass.equals(Date.class)) {
            return validateDateRange(fieldDefinition, (Date) value, validationResults);
        }

        return true;
    }

    private boolean validateDateRange(final DataFieldDefinition fieldDefinition, final Date value,
            final ValidationResults validationResults) {
        if (from != null && value.before((Date) from)) {
            addError(fieldDefinition, validationResults);
            return false;
        }
        if (to != null && value.after((Date) to)) {
            addError(fieldDefinition, validationResults);
            return false;
        }
        return true;
    }

    private boolean validateNumberRange(final DataFieldDefinition fieldDefinition, final Number value,
            final ValidationResults validationResults) {
        if (from != null && value.doubleValue() < ((Number) from).doubleValue()) {
            addError(fieldDefinition, validationResults);
            return false;
        }
        if (to != null && value.doubleValue() > ((Number) to).doubleValue()) {
            addError(fieldDefinition, validationResults);
            return false;
        }
        return true;
    }

    private boolean validateStringRange(final DataFieldDefinition fieldDefinition, final String value,
            final ValidationResults validationResults) {
        if (from != null && value.compareTo((String) from) < 0) {
            addError(fieldDefinition, validationResults);
            return false;
        }
        if (to != null && value.compareTo((String) to) > 0) {
            addError(fieldDefinition, validationResults);
            return false;
        }
        return true;
    }

    private void addError(final DataFieldDefinition fieldDefinition, final ValidationResults validationResults) {
        validationResults.addError(fieldDefinition, errorMessage, String.valueOf(from), String.valueOf(to));
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final DataFieldDefinition fieldDefinition, final Entity entity,
            final ValidationResults validationResults) {
        return true;
    }

    @Override
    public FieldValidator customErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }
}
