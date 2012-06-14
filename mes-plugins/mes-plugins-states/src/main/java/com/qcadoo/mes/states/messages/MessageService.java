package com.qcadoo.mes.states.messages;

import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.messages.constants.MessageType;
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
    Entity createMessage(final MessageType type, final String correspondField, final String translationKey,
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
    void addMessage(final Entity stateChangeEntity, final StateChangeEntityDescriber describer, final Entity message);

    /**
     * Create & add message to state change entity
     * 
     * @param stateChangeEntity
     * @param type
     * @param translationKey
     * @param translationArgs
     */
    void addMessage(final Entity stateChangeEntity, final StateChangeEntityDescriber describer, final MessageType type,
            final String translationKey, final String... translationArgs);

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
    void addMessage(final Entity stateChangeEntity, final StateChangeEntityDescriber describer, final MessageType type,
            final String correspondFieldName, final String translationKey, final String... translationArgs);

    /**
     * Create & add validation error message to state change entity
     * 
     * @param stateChangeEntity
     * @param describer
     * @param correspondField
     * @param translationKey
     * @param translationArgs
     */
    void addValidationError(final Entity stateChangeEntity, final StateChangeEntityDescriber describer,
            final String correspondField, final String translationKey, final String... translationArgs);
}
