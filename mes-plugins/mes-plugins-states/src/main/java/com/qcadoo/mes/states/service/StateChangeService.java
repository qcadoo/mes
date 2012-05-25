package com.qcadoo.mes.states.service;

import com.qcadoo.model.api.Entity;

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
    public void changeState(final Entity stateChangeEntity);

}
