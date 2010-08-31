package com.qcadoo.mes.core.data.internal.validators;

import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class RequiredValidator implements FieldValidator {

    private String errorMessage = "form.validate.errors.isRequired";

    @Override
    public boolean validate(final FieldDefinition fieldDefinition, final Object value, final ValidationResults validationResults) {
        if (value == null) {
            validationResults.addError(fieldDefinition, errorMessage);
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
