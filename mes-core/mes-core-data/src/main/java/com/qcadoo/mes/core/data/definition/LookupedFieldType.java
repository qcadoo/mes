package com.qcadoo.mes.core.data.definition;

import java.util.List;

/**
 * Method is {@link LookupedFieldType#lookup(String)} returns all possible values that begin with given prefix.
 */
public interface LookupedFieldType extends FieldType {

    List<String> lookup(String prefix);

}
