/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.model.api;

import java.util.Locale;

import com.qcadoo.mes.model.types.FieldType;

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
    String getValue(final Object value, Locale locale);

    /**
     * Return field's type.
     * 
     * @return field's type
     */
    FieldType getType();

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
     * Get expression to get the field value.
     * 
     * @return expression
     */
    String getExpression();

    /**
     * Return data definition which this field belongs to.
     * 
     * @return data definition
     */
    DataDefinition getDataDefinition();

}
