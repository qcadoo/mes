package com.qcadoo.mes.core.data.types;

import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public interface FieldType {

    boolean isSearchable();

    boolean isOrderable();

    boolean isAggregable();

    Class<?> getType();

    Object toObject(DataFieldDefinition fieldDefinition, Object value, ValidationResults validationResults);

    String toString(Object value);

}
