package com.qcadoo.mes.states.service.client;

import com.qcadoo.model.api.Entity;

/**
 * State change client for Samples plugin.
 * 
 * @since marcinkubala
 */
public interface StateChangeSamplesClient {

    /**
     * @param entity
     *            an entity whose state will be changed
     * @param targetState
     *            target (new) state
     * @return entity whose state was changed
     */
    Entity changeState(final Entity entity, final String targetState);
}
