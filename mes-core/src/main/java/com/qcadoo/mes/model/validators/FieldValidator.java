/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.validators;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;

/**
 * Validator takes value of the field and returns false in case of any error.
 */
public interface FieldValidator {

    /**
     * Validate field's value.
     * 
     * @param dataDefinition
     *            data definition
     * @param fieldDefinition
     *            field definition
     * @param value
     *            field's value
     * @param validatedEntity
     *            entity
     * @return true if field is valid
     */
    boolean validate(DataDefinition dataDefinition, FieldDefinition fieldDefinition, Object value, Entity validatedEntity);

    /**
     * Validate field's value of given entity.
     * 
     * @param dataDefinition
     *            data definition
     * @param fieldDefinition
     *            field definition
     * @param entity
     *            entity
     * @return true if field is valid
     */
    boolean validate(DataDefinition dataDefinition, FieldDefinition fieldDefinition, Entity entity);

    /**
     * Set custom message for this validator.
     * 
     * @param message
     *            message
     * @return this validator
     */
    FieldValidator customErrorMessage(String message);
}
