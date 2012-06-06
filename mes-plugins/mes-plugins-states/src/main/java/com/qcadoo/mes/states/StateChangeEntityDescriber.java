package com.qcadoo.mes.states;

import com.qcadoo.model.api.DataDefinition;

/**
 * State change entity describer is a kind of adapter for different models describing specific state change entities.
 * 
 * @since 1.1.7
 */
public interface StateChangeEntityDescriber {

    /**
     * @return data definition for state change entity.
     */
    DataDefinition getDataDefinition();

    /**
     * Parse string value into proper enum object.
     * 
     * @param stringValue
     * @return
     */
    Object parseStateEnum(final String stringValue);

    /**
     * @return name of belongsTo field pointing to owner entity.
     */
    String getOwnerFieldName();

    /**
     * @return name of string/enum field containing source (old) state value.
     */
    String getSourceStateFieldName();

    /**
     * @return name of string/enum field containing target (new) state value.
     */
    String getTargetStateFieldName();

    /**
     * @return name of boolean field containing finished flag.
     */
    String getFinishedFieldName();

    /**
     * @return name of hasMany field containing collection of related messages.
     */
    String getMessagesFieldName();

    /**
     * @return name of Integer field containing number of phase.
     */
    String getPhaseFieldName();
}
