package com.qcadoo.mes.core.data.validation;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.ModelDefinition;
import com.qcadoo.mes.core.data.model.FieldDefinition;

/**
 * Validator takes value of the field and returns true in case of no errors.
 */
public interface FieldValidator {

    boolean validate(ModelDefinition dataDefinition, FieldDefinition fieldDefinition, Object value, Entity validatedEntity);

    boolean validate(ModelDefinition dataDefinition, FieldDefinition fieldDefinition, Entity entity);

    FieldValidator customErrorMessage(String message);
}
