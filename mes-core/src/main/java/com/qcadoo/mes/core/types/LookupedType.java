package com.qcadoo.mes.core.types;

import java.util.Map;


/**
 * Method is {@link LookupedType#lookup(String)} returns all possible values that begin with given prefix.
 */
public interface LookupedType extends FieldType {

    Map<Long, String> lookup(String prefix);

}
