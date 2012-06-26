package com.qcadoo.mes.states.service;

import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.model.api.Entity;

/**
 * Service for creating state change entities
 * 
 * @since 1.1.7
 */
public interface StateChangeEntityBuilder {

    /**
     * Build state change entity for specified (not initial) state change
     * 
     * @param describer
     *            state change entity describer.
     * @param owner
     *            entity whose state will be changed.
     * @param targetState
     *            new state value.
     * @return new, not persisted state change entity
     */
    Entity build(final StateChangeEntityDescriber describer, final Entity owner, final StateEnum targetState);

    /**
     * Build state change entity for initial state.
     * 
     * @param describer
     *            state change entity describer.
     * @param initialState
     *            state enum value.
     * @return new, not persisted, initial state set entity.
     */
    Entity buildInitial(final StateChangeEntityDescriber describer, final Entity owner, final StateEnum initialState);

}
