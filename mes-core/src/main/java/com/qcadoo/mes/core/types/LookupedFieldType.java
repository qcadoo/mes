package com.qcadoo.mes.core.types;

import java.util.Map;


/**
 * Method is {@link LookupedFieldType#lookup(String)} returns all possible values that begin with given prefix.
 */
public interface LookupedFieldType extends FieldType {

    Map<Long, String> lookup(String prefix);

}
