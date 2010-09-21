package com.qcadoo.mes.core.data.types;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.FieldDefinition;

public interface FieldType {

    boolean isSearchable();

    boolean isOrderable();

    boolean isAggregable();

    Class<?> getType();

    Object toObject(FieldDefinition fieldDefinition, Object value, Entity validatedEntity);

    String toString(Object value);

}
