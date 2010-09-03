package com.qcadoo.mes.core.data.internal.validators;

import java.math.BigDecimal;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class MaxLenghtValidator implements FieldValidator {

    private static final String LENGTH_EXCEEDED_ERROR = "core.validation.error.maxLengthExceeded";

    private final int maxLength;

    private String errorMessage = LENGTH_EXCEEDED_ERROR;

    public MaxLenghtValidator(final int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        if (value == null) {
            return true;
        }

        Class<?> fieldClass = fieldDefinition.getType().getType();

        if (!(fieldClass.equals(String.class) || fieldClass.equals(Integer.class) || fieldClass.equals(BigDecimal.class))) {
            return true;
        }

        if (value.toString().length() > maxLength) {
            validationResults.addError(fieldDefinition, errorMessage, String.valueOf(maxLength));
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
