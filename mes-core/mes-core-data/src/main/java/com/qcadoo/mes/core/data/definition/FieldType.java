package com.qcadoo.mes.core.data.definition;

/**
 * Method is {@link FieldType#isValidType(Object)} returns true is value has proper type.
 */
public interface FieldType {

    int NUMERIC_TYPE_BOOLEAN = 1;

    int NUMERIC_TYPE_DATE = 1;

    int NUMERIC_TYPE_DATE_TIME = 1;

    int NUMERIC_TYPE_DICTIONARY = 1;

    int NUMERIC_TYPE_ENUM = 1;

    int NUMERIC_TYPE_INTEGER = 1;

    int NUMERIC_TYPE_DECIMAL = 1;

    int NUMERIC_TYPE_STRING = 1;

    int NUMERIC_TYPE_TEXT = 1;

    int getNumericType();

    boolean isSearchable();

    boolean isOrderable();

    boolean isAggregable();

    boolean isValidType(Object value);

}
