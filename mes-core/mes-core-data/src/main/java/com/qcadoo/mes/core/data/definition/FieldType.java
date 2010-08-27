package com.qcadoo.mes.core.data.definition;

public interface FieldType {

    int getNumericType();

    boolean isSearchable();

    boolean isOrderable();

    boolean isAggregable();

}
