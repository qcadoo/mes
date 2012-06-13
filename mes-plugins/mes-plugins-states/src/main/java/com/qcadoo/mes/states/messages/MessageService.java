package com.qcadoo.mes.states.messages;

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
}
