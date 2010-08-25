package com.qcadoo.mes.core.data.definition;

import java.util.Map;

/**
 * Method is {@link LookupedFieldType#lookup(String)} returns all possible values that begin with given prefix.
 */
public interface LookupedFieldType extends FieldType {

    Map<Long, String> lookup(String prefix);

}
