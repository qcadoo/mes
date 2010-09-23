package com.qcadoo.mes.core.internal.validators;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.model.DataDefinition;
import com.qcadoo.mes.core.model.HookDefinition;
import com.qcadoo.mes.core.validation.EntityValidator;

public final class CustomEntityValidator implements EntityValidator {

    private static final String CUSTOM_ERROR = "commons.validate.field.error.customEntity";

    private final HookDefinition entityValidateHook;

    private String errorMessage = CUSTOM_ERROR;

    public CustomEntityValidator(final HookDefinition entityValidateHook) {
        this.entityValidateHook = entityValidateHook;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final Entity entity) {
        boolean result = entityValidateHook.callWithEntityAndGetBoolean(dataDefinition, entity);
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
