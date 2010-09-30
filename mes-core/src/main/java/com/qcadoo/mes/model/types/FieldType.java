package com.qcadoo.mes.model.types;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;

public interface FieldType {

    boolean isSearchable();

    boolean isOrderable();

    boolean isAggregable();

    Class<?> getType();

    Object toObject(FieldDefinition fieldDefinition, Object value, Entity validatedEntity);

    String toString(Object value);

}
