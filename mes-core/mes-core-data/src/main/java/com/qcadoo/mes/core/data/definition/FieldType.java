package com.qcadoo.mes.core.data.definition;

/**
 * Method is {@link FieldType#isValidType(Object)} returns true is value has proper type.
 */
public interface FieldType {

    int getNumericType();

    boolean isSearchable();

    boolean isOrderable();

    boolean isAggregable();

    boolean isValidType(Object value);

}
