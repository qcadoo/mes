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

package com.qcadoo.mes.model;

import java.util.List;

import com.qcadoo.mes.model.types.FieldType;
import com.qcadoo.mes.model.validators.FieldValidator;

/**
 * Object defines database field.
 * 
 * @apiviz.has com.qcadoo.mes.core.data.definition.FieldType
 * @apiviz.owns com.qcadoo.mes.core.data.definition.FieldValidator
 * @apiviz.has com.qcadoo.mes.model.DataDefinition
 */
public interface FieldDefinition {

    /**
     * Return field's name.
     * 
     * @return field's name
     */
    String getName();

    /**
     * {@link FieldType#toString(Object)}
     */
    String getValue(final Object value);

    /**
     * Return field's type.
     * 
     * @return field's type
     */
    FieldType getType();

    /**
     * Return all defined field's validators.
     * 
     * @return field's validators
     */
    List<FieldValidator> getValidators();

    /**
     * Return true if this field is readonly on update.
     * 
     * @return is readonly on update
     */
    boolean isReadOnlyOnUpdate();

    /**
     * Return true if this field is readonly.
     * 
     * @return is readonly
     */
    boolean isReadOnly();

    /**
     * Return true if this field is required.
     * 
     * @return is required
     */
    boolean isRequired();

    /**
     * Return true if this field is required on create.
     * 
     * @return is required on create
     */
    boolean isRequiredOnCreate();

    /**
     * Return default value for this field.
     * 
     * @return default value
     */
    Object getDefaultValue();

    /**
     * Return true if this field is unique.
     * 
     * @return is unique
     */
    boolean isUnique();

    /**
     * Return true if this field is unique persistent (will be saved in database).
     * 
     * @return is persistent
     */
    boolean isPersistent();

    /**
     * Return data definition which this field belongs to.
     * 
     * @return data definition
     */
    DataDefinition getDataDefinition();

}
