package com.qcadoo.mes.states;

import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.model.api.Entity;

/**
 * State change context holder obect
 * 
 * @since 1.1.7
 */
public interface StateChangeContext extends StateChangeMessagesHolder {

    /**
     * Save inner state change entity
     */
    public void save();

    /**
     * Set inner state change entity's field
     * 
     * @param fieldName
     * @param fieldValue
     */
    public void setField(final String fieldName, final Object fieldValue);

    /**
     * Returns state enum value from field with given name
     * 
     * @param fieldName
     * @return
     */
    public StateEnum getStateEnumValue(final String fieldName);

    /**
     * Returns current phase number
     * 
     * @return current phase number
     */
    public int getPhase();

    /**
     * Set current phase number
     * 
     * @param phase
     */
    public void setPhase(final int phase);

    /**
     * Returns state change entity's status
     * 
     * @return state change entity's status
     */
    public StateChangeStatus getStatus();

    /**
     * Set state change entity's status
     * 
     * @param status
     *            state change entity's status
     */
    public void setStatus(final StateChangeStatus status);

    /**
     * Returns state change entity's describer
     * 
     * @return state change entity's describer
     */
    public StateChangeEntityDescriber getDescriber();

    /**
     * Returns inner state change entity
     * 
     * @return inner state change entity
     */
    public Entity getEntity();

    /**
     * Returns inner state change entity's owner
     * 
     * @return inner state change entity's owner
     */
    public Entity getOwner();

}
