package com.qcadoo.mes.model.types;

import com.qcadoo.mes.model.DataDefinition;

public interface HasManyType extends FieldType {

    String getJoinFieldName();

    DataDefinition getDataDefinition();

}