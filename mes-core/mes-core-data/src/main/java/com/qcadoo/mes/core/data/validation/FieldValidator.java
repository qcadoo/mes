package com.qcadoo.mes.core.data.validation;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;

/**
 * Validator takes value of the field and returns true in case of no errors.
 */
public interface FieldValidator {

    boolean validate(DataDefinition dataDefinition, DataFieldDefinition fieldDefinition, Object value,
            ValidationResults validationResults);

    boolean validate(DataDefinition dataDefinition, DataFieldDefinition fieldDefinition, Entity entity,
            ValidationResults validationResults);

    FieldValidator customErrorMessage(String message);
}
