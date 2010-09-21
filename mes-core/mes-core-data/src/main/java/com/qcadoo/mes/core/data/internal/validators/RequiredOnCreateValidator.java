package com.qcadoo.mes.core.data.internal.validators;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.ModelDefinition;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.validation.FieldValidator;

public final class RequiredOnCreateValidator implements FieldValidator {

    private static final String MISSING_ERROR = "commons.validate.field.error.missing";

    private String errorMessage = MISSING_ERROR;

    @Override
    public boolean validate(final ModelDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final Entity validatedEntity) {
        return true;
    }

    @Override
    public boolean validate(final ModelDefinition dataDefinition, final FieldDefinition fieldDefinition, final Entity entity) {
        if (entity.getId() == null && entity.getField(fieldDefinition.getName()) == null) {
            entity.addError(fieldDefinition, errorMessage);
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
