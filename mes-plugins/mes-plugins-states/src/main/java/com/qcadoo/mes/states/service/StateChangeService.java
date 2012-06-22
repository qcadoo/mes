package com.qcadoo.mes.states.service;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;

/**
 * Service for changing state of entity
 * 
 * @since 1.1.7
 */
public interface StateChangeService {

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
