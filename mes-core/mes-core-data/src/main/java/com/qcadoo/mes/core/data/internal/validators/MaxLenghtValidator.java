package com.qcadoo.mes.core.data.internal.validators;

import java.math.BigDecimal;

import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class MaxLenghtValidator implements FieldValidator {

    private static final String MAX_LENGTH_EXCEEDED_ERROR = "core.validation.error.maxLengthExceeded";

    private final int maxLenght;

    private String errorMessage = MAX_LENGTH_EXCEEDED_ERROR;

    public MaxLenghtValidator(final int maxLenght) {
        this.maxLenght = maxLenght;
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

        if (value.toString().length() > maxLenght) {
            validationResults.addError(fieldDefinition, errorMessage, String.valueOf(maxLenght));
            return false;
        }

        return true;
    }

    @Override
    public FieldValidator customErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

}
