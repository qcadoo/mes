package com.qcadoo.mes.core.data.internal.validators;

import java.math.BigDecimal;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class MaxPrecisionAndScaleValidator implements FieldValidator {

    private static final String PRECISION_AND_SCALE_EXCEEDED_ERROR = "commons.validate.field.error.maxPrecisionAndScaleExceeded";

    private final int maxScale;

    private final int maxPrecision;

    private String errorMessage = PRECISION_AND_SCALE_EXCEEDED_ERROR;

    public MaxPrecisionAndScaleValidator(final int maxPrecision, final int maxScale) {
        this.maxPrecision = maxPrecision;
        this.maxScale = maxScale;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        if (value == null) {
            return true;
        }

        Class<?> fieldClass = fieldDefinition.getType().getType();

        if (!(fieldClass.equals(Integer.class) || fieldClass.equals(BigDecimal.class))) {
            return true;
        }

        if (fieldClass.equals(BigDecimal.class)) {
            return validateDecimal(fieldDefinition, validationResults, (BigDecimal) value);
        } else {
            return validateInteger(fieldDefinition, value, validationResults);
        }
    }

    private boolean validateInteger(final FieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        if (value.toString().length() > maxPrecision) {
            validationResults.addError(fieldDefinition, errorMessage, String.valueOf(maxPrecision), String.valueOf(maxScale));
            return false;
        } else {
            return true;
        }
    }

    private boolean validateDecimal(final FieldDefinition fieldDefinition, final ValidationResults validationResults,
            final BigDecimal decimal) {
        if (decimal.precision() - decimal.scale() > maxPrecision) {
            validationResults.addError(fieldDefinition, errorMessage, String.valueOf(maxPrecision), String.valueOf(maxScale));
            return false;
        }
        if (maxScale > 0 && decimal.scale() > maxScale) {
            validationResults.addError(fieldDefinition, errorMessage, String.valueOf(maxPrecision), String.valueOf(maxScale));
            return false;
        }

        return true;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Entity entity,
            final ValidationResults validationResults) {
        return true;
    }

    @Override
    public FieldValidator customErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

}
