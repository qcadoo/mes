package com.qcadoo.mes.states.service;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.Entity;

/**
 * Service for changing state of entity
 * 
 * @since 1.1.7
 */
public interface StateChangeService {

    /**
     * Create new state change context. This method already create and persist state change entity.
     * 
     * @param entity
     *            entity which state you want to be changed
     * @return state change context object
     */
    StateChangeContext buildStateChangeContext(final Entity owner, final String targetStateString);

    /**
     * Build state change context from given state change entity.
     * 
     * @param stateChangeEntity
     * @return state change context object
     */
    StateChangeContext buildStateChangeContext(final Entity stateChangeEntity);

    /**
     * Perform state change.
     * 
     * @param stateChangeEntity
     *            entity which persist state change flow
     */
    void changeState(final StateChangeContext stateChangeContext);

    /**
     * Returns instance of appropriate {@link StateChangeEntityDescriber} which describe used state change entity model.
     * 
     * @return state change entity describer object
     */
    StateChangeEntityDescriber getChangeEntityDescriber();

}
