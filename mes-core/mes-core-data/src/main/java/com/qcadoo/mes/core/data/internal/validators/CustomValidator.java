package com.qcadoo.mes.core.data.internal.validators;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.HookDefinition;
import com.qcadoo.mes.core.data.model.ModelDefinition;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.validation.FieldValidator;

public final class CustomValidator implements FieldValidator {

    private static final String CUSTOM_ERROR = "commons.validate.field.error.custom";

    private final HookDefinition callback;

    private String errorMessage = CUSTOM_ERROR;

    public CustomValidator(final HookDefinition callback) {
        this.callback = callback;
    }

    @Override
    public boolean validate(final ModelDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final Entity validatedEntity) {
        boolean result = callback.callWithObjectAndGetBoolean(dataDefinition, value);
        if (result) {
            return true;
        }
        validatedEntity.addError(fieldDefinition, errorMessage);
        return false;
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
