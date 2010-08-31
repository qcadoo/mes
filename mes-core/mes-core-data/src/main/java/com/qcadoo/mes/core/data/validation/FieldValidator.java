package com.qcadoo.mes.core.data.validation;

import com.qcadoo.mes.core.data.definition.FieldDefinition;

/**
 * Validator takes value of the field and returns null in case of no errors or error message.
 */
public interface FieldValidator {

    boolean validate(FieldDefinition fieldDefinition, Object value, ValidationResults validationResults);

    FieldValidator customErrorMessage(String message);
}
