package com.qcadoo.mes.core.data.internal.validators;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.CallbackDefinition;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.validation.EntityValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class CustomEntityValidator implements EntityValidator {

    private static final String CUSTOM_ERROR = "core.validation.error.customEntity";

    private final CallbackDefinition callback;

    private String errorMessage = CUSTOM_ERROR;

    public CustomEntityValidator(final CallbackDefinition callback) {
        this.callback = callback;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final Entity entity, final ValidationResults validationResults) {
        boolean result = callback.callWithEntityAndGetBoolean(entity);
        if (result) {
            return true;
        }
        validationResults.addGlobalError(errorMessage);
        return false;
    }

    @Override
    public EntityValidator customErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

}
