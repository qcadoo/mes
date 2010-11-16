/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.types;

import java.util.List;

/**
 * Object represents "enum" field type.
 */
public interface EnumeratedType extends FieldType {

    /**
     * Return all possible field values.
     * 
     * @return values
     */
    List<String> values();
}
