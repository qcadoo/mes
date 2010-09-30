package com.qcadoo.mes.model.types;

import java.util.List;

/**
 * Method is {@link EnumeratedType#values()} returns all possible values.
 */
public interface EnumeratedType extends FieldType {

    List<String> values();
}
