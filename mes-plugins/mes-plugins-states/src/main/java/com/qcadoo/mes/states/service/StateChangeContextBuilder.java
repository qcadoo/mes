package com.qcadoo.mes.states.service;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.Entity;

public interface StateChangeContextBuilder {

    /**
     * Create new state change context. This method already create and persist state change entity.
     * 
     * @param describer
     * @param ownerEntity
     *            entity which state you want to be changed (state change's owner)
     * @return state change context object
     */
    StateChangeContext build(final StateChangeEntityDescriber describer, final Entity ownerEntity,
            final String targetStateString);

    /**
     * Build state change context from given state change entity.
     * 
     * @param describer
     * @param stateChangeEntity
     * @return state change context object
     */
    StateChangeContext build(final StateChangeEntityDescriber describer, final Entity stateChangeEntity);

}
