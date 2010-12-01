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

package com.qcadoo.mes.model.types;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;

/**
 * Object represents field type.
 */
public interface FieldType {

    /**
     * Return true if field is searchable.
     * 
     * @return is searchable
     */
    boolean isSearchable();

    /**
     * Return true if field is orderable.
     * 
     * @return is orderable
     */
    boolean isOrderable();

    /**
     * Return true if field is aggregable.
     * 
     * @return is aggregable
     */
    boolean isAggregable();

    /**
     * Return field class.
     * 
     * @return class
     */
    Class<?> getType();

    /**
     * Convert given value to valid field's value.
     * 
     * @param fieldDefinition
     *            field definition
     * @param value
     *            value
     * @param validatedEntity
     *            entity
     * @return valid value
     */
    Object toObject(FieldDefinition fieldDefinition, Object value, Entity validatedEntity);

    /**
     * Convert field's value to string.
     * 
     * @param value
     *            value
     * @return string value
     */
    String toString(Object value);

}
