package com.qcadoo.mes.core.validation;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.model.DataDefinition;

/**
 * Validator takes value of the entity and returns true in case of no errors.
 */
public interface EntityValidator {

    boolean validate(DataDefinition dataDefinition, Entity entity);

    EntityValidator customErrorMessage(String message);
}
