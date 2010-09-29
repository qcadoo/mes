package com.qcadoo.mes.core.types;

import java.util.List;

/**
 * Method is {@link EnumeratedFieldType#values()} returns all possible values.
 */
public interface EnumeratedFieldType extends FieldType {

    List<String> values();
}
