package com.qcadoo.mes.model.types;

import com.qcadoo.mes.model.DataDefinition;

public interface HasManyType extends FieldType {

    enum Cascade {
        NULLIFY, DELETE
    }

    String getJoinFieldName();

    DataDefinition getDataDefinition();

    Cascade getCascade();

}