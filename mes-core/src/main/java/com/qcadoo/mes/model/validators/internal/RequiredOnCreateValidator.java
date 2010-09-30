package com.qcadoo.mes.model.validators.internal;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.validation.FieldValidator;

public final class RequiredOnCreateValidator implements FieldValidator {

    private static final String MISSING_ERROR = "commons.validate.field.error.missing";

    private String errorMessage = MISSING_ERROR;

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final Entity validatedEntity) {
        return true;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Entity entity) {
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
