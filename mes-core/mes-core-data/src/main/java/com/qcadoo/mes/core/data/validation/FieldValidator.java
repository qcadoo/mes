package com.qcadoo.mes.core.data.validation;

import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;

/**
 * Validator takes value of the field and returns true in case of no errors.
 */
public interface FieldValidator {

    boolean validate(DataDefinition dataDefinition, FieldDefinition fieldDefinition, Object value,
            ValidationResults validationResults);

    FieldValidator customErrorMessage(String message);
}
