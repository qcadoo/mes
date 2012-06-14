package com.qcadoo.mes.states.messages;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.Entity;

/**
 * State change messages service
 * 
 * @since 1.1.7
 */
public interface MessageService {

    /**
     * Create new message entity
     * 
     * @param type
     *            type of message
     * @param correspondField
     * @param translationKey
     * @param translationArgs
     * @return newly created message entity (not persisted yet!)
     */
    Entity createMessage(final String translationKey, final StateMessageType type, final String correspondField,
            final String... translationArgs);

    /**
     * Check if given message entity already exists
     * 
     * @param message
     *            entity you are looking for.
     * @return true if at least one equal entity was found.
     */
    boolean messageAlreadyExists(final Entity message);

    /**
     * Add message to state change entity
     * 
     * @param stateChangeEntity
     * @param message
     */
    void addMessage(final StateChangeContext stateChangeContext, final Entity message);

    /**
     * Create & add message to state change entity
     * 
     * @param stateChangeEntity
     * @param describer
     * @param type
     * @param correspondFieldName
     * @param translationKey
     * @param translationArgs
     */
    void addMessage(final StateChangeContext stateChangeContext, final StateMessageType type, final String correspondFieldName,
            final String translationKey, final String... translationArgs);

    /**
     * Create & add validation error message to state change entity
     * 
     * @param stateChangeEntity
     * @param describer
     * @param correspondField
     * @param translationKey
     * @param translationArgs
     */
    void addValidationError(final StateChangeContext stateChangeContext, final String correspondField,
            final String translationKey, final String... translationArgs);
}
