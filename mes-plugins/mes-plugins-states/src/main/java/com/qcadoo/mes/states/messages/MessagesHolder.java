package com.qcadoo.mes.states.messages;

import java.util.List;

import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.Entity;

public interface MessagesHolder {

    void addFieldMessage(final String translationKey, final StateMessageType type, final String fieldName,
            final String... translationArgs);

    void addMessage(final String translationKey, final StateMessageType type, final String... translationArgs);

    void addMessage(final String translationKey, final StateMessageType type, final boolean autoClose,
            final String... translationArgs);

    void addFieldValidationError(final String translationKey, final String fieldName, final String... translationArgs);

    void addValidationError(final String translationKey, final String... translationArgs);

    List<Entity> getAllMessages();

    MessageService getMessageService();
}
