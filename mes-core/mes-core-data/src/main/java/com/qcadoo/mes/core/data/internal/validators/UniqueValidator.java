package com.qcadoo.mes.core.data.internal.validators;

import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class UniqueValidator implements FieldValidator {

    private static final String UNIQUE_ERROR = "core.validation.error.duplicated";

    private String errorMessage = UNIQUE_ERROR;

    @Override
    public boolean validate(final FieldDefinition fieldDefinition, final Object value, final ValidationResults validationResults) {
        // TODO masz
        return true;
    }

    @Override
    public FieldValidator customErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

}
