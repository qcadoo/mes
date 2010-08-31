package com.qcadoo.mes.core.data.types;

import java.util.List;


/**
 * Method is {@link EnumeratedFieldType#values()} returns all possible values.
 */
public interface EnumeratedFieldType extends FieldType {

    List<String> values();
}
