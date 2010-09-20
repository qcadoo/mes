package com.qcadoo.mes.core.data.internal.validators;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.HookDefinition;
import com.qcadoo.mes.core.data.model.ModelDefinition;
import com.qcadoo.mes.core.data.validation.EntityValidator;

public final class CustomEntityValidator implements EntityValidator {

    private static final String CUSTOM_ERROR = "commons.validate.field.error.customEntity";

    private final HookDefinition callback;

    private String errorMessage = CUSTOM_ERROR;

    public CustomEntityValidator(final HookDefinition callback) {
        this.callback = callback;
    }

    @Override
    public boolean validate(final ModelDefinition dataDefinition, final Entity entity) {
        boolean result = callback.callWithEntityAndGetBoolean(dataDefinition, entity);
        if (result) {
            return true;
        }
        entity.addGlobalError(errorMessage);
        return false;
    }

    @Override
    public EntityValidator customErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

}
