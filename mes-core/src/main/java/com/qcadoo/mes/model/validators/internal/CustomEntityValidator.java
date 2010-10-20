package com.qcadoo.mes.model.validators.internal;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.model.validators.EntityValidator;

public final class CustomEntityValidator implements EntityValidator {

    private static final String CUSTOM_ERROR = "commons.validate.global.error.custom";

    private final HookDefinition entityValidateHook;

    public CustomEntityValidator(final HookDefinition entityValidateHook) {
        this.entityValidateHook = entityValidateHook;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final Entity entity) {
        boolean result = entityValidateHook.callWithEntityAndGetBoolean(dataDefinition, entity);
        if (result) {
            return true;
        }
        if (entity.isValid()) {
            entity.addGlobalError(CUSTOM_ERROR);
        }
        return false;
    }

}
