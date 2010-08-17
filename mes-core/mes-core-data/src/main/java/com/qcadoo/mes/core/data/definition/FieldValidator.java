package com.qcadoo.mes.core.data.definition;

/**
 * Validator takes value of the field and returns null in case of no errors or error message.
 */
public interface FieldValidator {

    String validate(Object fieldValue);

}
