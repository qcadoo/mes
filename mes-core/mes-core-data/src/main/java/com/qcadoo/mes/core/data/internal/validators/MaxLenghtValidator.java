package com.qcadoo.mes.core.data.internal.validators;

import java.math.BigDecimal;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.ModelDefinition;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.validation.FieldValidator;

public final class MaxLenghtValidator implements FieldValidator {

    private static final String LENGTH_EXCEEDED_ERROR = "commons.validate.field.error.maxLengthExceeded";

    private final int maxLength;

    private String errorMessage = LENGTH_EXCEEDED_ERROR;

    public MaxLenghtValidator(final int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public boolean validate(final ModelDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final Entity validatedEntity) {
        if (value == null) {
            return true;
        }

        Class<?> fieldClass = fieldDefinition.getType().getType();

        if (!(fieldClass.equals(String.class) || fieldClass.equals(Integer.class) || fieldClass.equals(BigDecimal.class))) {
            return true;
        }

        if (value.toString().length() > maxLength) {
            validatedEntity.addError(fieldDefinition, errorMessage, String.valueOf(maxLength));
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
