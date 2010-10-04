package com.qcadoo.mes.model.validators.internal;

import java.math.BigDecimal;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.validators.FieldValidator;

public final class ScaleValidator implements FieldValidator {

    private static final String INVALID_SCALE_ERROR = "commons.validate.field.error.invalidScale";

    private final Integer max;

    private final Integer min;

    private final Integer is;

    private String errorMessage = INVALID_SCALE_ERROR;

    public ScaleValidator(final Integer min, final Integer is, final Integer max) {
        this.min = min;
        this.is = is;
        this.max = max;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final Entity validatedEntity) {
        if (value == null) {
            return true;
        }

        Class<?> fieldClass = fieldDefinition.getType().getType();

        if (!fieldClass.equals(BigDecimal.class)) {
            return true;
        }

        return validateScale(fieldDefinition, value, validatedEntity);
    }

    private boolean validateScale(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        int scale = ((BigDecimal) value).scale();

        if (max != null && scale > max) {
            validatedEntity.addError(fieldDefinition, errorMessage);
            return false;
        }
        if (min != null && scale < min) {
            validatedEntity.addError(fieldDefinition, errorMessage);
            return false;
        }
        if (is != null && !is.equals(scale)) {
            validatedEntity.addError(fieldDefinition, errorMessage);
            return false;
        }

        return true;
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
