package com.qcadoo.mes.core.data.validation;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;

/**
 * Validator takes value of the entity and returns true in case of no errors.
 */
public interface EntityValidator {

    boolean validate(DataDefinition dataDefinition, Entity entity, ValidationResults validationResults);

    EntityValidator customErrorMessage(String message);
}
