/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
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
