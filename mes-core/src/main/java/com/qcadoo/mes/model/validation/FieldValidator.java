package com.qcadoo.mes.model.validation;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;

/**
 * Validator takes value of the field and returns true in case of no errors.
 */
public interface FieldValidator {

    boolean validate(DataDefinition dataDefinition, FieldDefinition fieldDefinition, Object value, Entity validatedEntity);

    boolean validate(DataDefinition dataDefinition, FieldDefinition fieldDefinition, Entity entity);

    FieldValidator customErrorMessage(String message);
}
