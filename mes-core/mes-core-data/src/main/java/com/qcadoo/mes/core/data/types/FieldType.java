package com.qcadoo.mes.core.data.types;

import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public interface FieldType {

    int getNumericType();

    boolean isSearchable();

    boolean isOrderable();

    boolean isAggregable();

    Class<?> getType();

    Object fromString(FieldDefinition fieldDefinition, String value, ValidationResults validationResults);

    String toString(Object value);

    boolean validate(FieldDefinition fieldDefinition, Object value, ValidationResults validationResults);

}
