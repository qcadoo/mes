/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.types;

import java.util.Map;

/**
 * Object represents "lookup" field type.
 */
public interface LookupedType extends FieldType {

    /**
     * Return all possible field values matching prefix.
     * 
     * @return values
     */
    Map<Long, String> lookup(String prefix);

}
