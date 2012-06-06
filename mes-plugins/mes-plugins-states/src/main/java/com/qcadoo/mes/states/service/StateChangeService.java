package com.qcadoo.mes.states.service;

import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.model.api.Entity;

/**
 * Service for changing state of entity
 * 
 * @since 1.1.7
 */
public interface StateChangeService {

    /**
     * Create new state change entity
     * 
     * @param entity
     *            entity which state you want to be changed
     * @return stateChangeEntity
     */
    Entity createNewStateChangeEntity(final Entity entity, final String targetState);

    /**
     * Perform state change.
     * 
     * @param stateChangeEntity
     *            entity which persist state change flow
     */
    void changeState(final Entity stateChangeEntity);

    /**
     * Returns instance of appropriate {@link StateChangeEntityDescriber} which describe used state change entity model.
     * 
     * @return state change entity describer object
     */
    StateChangeEntityDescriber getChangeEntityDescriber();

    /**
     * Add message to state change entity
     * 
     * @param stateChangeEntity
     * @param message
     */
    void addMessage(final Entity stateChangeEntity, final Entity message);

    /**
     * Create & add message to state change entity
     * 
     * @param stateChangeEntity
     * @param type
     * @param translationKey
     * @param translationArgs
     */
    void addMessage(final Entity stateChangeEntity, final MessageType type, final String translationKey,
            final String... translationArgs);

}
