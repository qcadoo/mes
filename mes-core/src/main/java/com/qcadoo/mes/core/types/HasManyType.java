package com.qcadoo.mes.core.types;

import com.qcadoo.mes.core.model.DataDefinition;

public interface HasManyType extends FieldType {

    String getJoinFieldName();

    DataDefinition getDataDefinition();

}