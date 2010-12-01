/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
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
