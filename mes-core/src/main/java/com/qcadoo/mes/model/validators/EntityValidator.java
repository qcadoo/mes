/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.validators;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;

/**
 * Validator takes value of the entity and returns false in case of any error.
 */
public interface EntityValidator {

    /**
     * Validate entity.
     * 
     * @param dataDefinition
     *            data definition
     * @param entity
     *            entity
     * @return true if entity is valid
     */
    boolean validate(DataDefinition dataDefinition, Entity entity);

}