package com.qcadoo.mes.core.data.internal.validators;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.CallbackDefinition;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class CustomValidator implements FieldValidator {

    private static final String CUSTOM_ERROR = "commons.validate.field.error.custom";

    private final CallbackDefinition callback;

    private String errorMessage = CUSTOM_ERROR;

    public CustomValidator(final CallbackDefinition callback) {
        this.callback = callback;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        boolean result = callback.callWithObjectAndGetBoolean(value);
        if (result) {
            return true;
        }
        validationResults.addError(fieldDefinition, errorMessage);
        return false;
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
