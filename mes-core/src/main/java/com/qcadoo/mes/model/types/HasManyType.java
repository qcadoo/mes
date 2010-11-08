package com.qcadoo.mes.model.types;

import com.qcadoo.mes.model.DataDefinition;

/**
 * Object represents "has many" field type.
 */
public interface HasManyType extends FieldType {

    /**
     * Cascade type.
     */
    enum Cascade {
        NULLIFY, DELETE
    }

    /**
     * Return join field name.
     * 
     * @return join field
     */
    String getJoinFieldName();

    /**
     * Return data definition.
     * 
     * @return data definition
     */
    DataDefinition getDataDefinition();

    /**
     * Return cascade type.
     * 
     * @return cascade type
     */
    Cascade getCascade();

}