package com.qcadoo.mes.states.service;

import com.qcadoo.model.api.Entity;

/**
 * Service for changing state of entity
 * 
 * @since 1.1.7
 */
public interface StateChangeService {

    /**
     * create new state change entity
     * 
     * @return stateChangeEntity
     */
    Entity createStateChangeEntity();

    /**
     * Perform state change.
     * 
     * @param stateChangeEntity
     *            entity which persist state change flow
     */
    void changeState(final Entity stateChangeEntity);

}
