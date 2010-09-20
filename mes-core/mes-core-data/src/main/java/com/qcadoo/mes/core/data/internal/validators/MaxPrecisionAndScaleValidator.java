package com.qcadoo.mes.core.data.internal.validators;

import java.math.BigDecimal;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.ModelDefinition;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.validation.FieldValidator;

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
    public boolean validate(final ModelDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final Entity validatedEntity) {
        if (value == null) {
            return true;
        }

        Class<?> fieldClass = fieldDefinition.getType().getType();

        if (!(fieldClass.equals(Integer.class) || fieldClass.equals(BigDecimal.class))) {
            return true;
        }

        if (fieldClass.equals(BigDecimal.class)) {
            return validateDecimal(fieldDefinition, validatedEntity, (BigDecimal) value);
        } else {
            return validateInteger(fieldDefinition, value, validatedEntity);
        }
    }

    private boolean validateInteger(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        if (value.toString().length() > maxPrecision) {
            validatedEntity.addError(fieldDefinition, errorMessage, String.valueOf(maxPrecision), String.valueOf(maxScale));
            return false;
        } else {
            return true;
        }
    }

    private boolean validateDecimal(final FieldDefinition fieldDefinition, final Entity validatedEntity, final BigDecimal decimal) {
        if (decimal.precision() - decimal.scale() > maxPrecision) {
            validatedEntity.addError(fieldDefinition, errorMessage, String.valueOf(maxPrecision), String.valueOf(maxScale));
            return false;
        }
        if (maxScale > 0 && decimal.scale() > maxScale) {
            validatedEntity.addError(fieldDefinition, errorMessage, String.valueOf(maxPrecision), String.valueOf(maxScale));
            return false;
        }

        return true;
    }

    @Override
    public boolean validate(final ModelDefinition dataDefinition, final FieldDefinition fieldDefinition, final Entity entity) {
        return true;
    }

    @Override
    public FieldValidator customErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

}
