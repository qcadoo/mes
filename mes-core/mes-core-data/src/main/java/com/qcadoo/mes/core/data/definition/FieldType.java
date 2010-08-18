package com.qcadoo.mes.core.data.definition;

/**
 * Method is {@link FieldType#isValidType(Object)} returns true is value has proper type.
 */
public interface FieldType {

    boolean isSearchable();

    boolean isOrderable();

    boolean isAggregable();

    boolean isValidType(Object value);

}
