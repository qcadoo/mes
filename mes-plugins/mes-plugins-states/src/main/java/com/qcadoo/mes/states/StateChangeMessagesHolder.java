package com.qcadoo.mes.states;

import java.util.List;

import com.qcadoo.mes.states.messages.MessageService;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.model.api.Entity;

public interface StateChangeMessagesHolder {

    public void addFieldMessage(final String translationKey, final MessageType type, final String fieldName,
            final String... translationArgs);

    public void addMessage(final String translationKey, final MessageType type, final String... translationArgs);

    public void addFieldValidationError(final String translationKey, final String fieldName, final String... translationArgs);

    public void addValidationError(final String translationKey, final String... translationArgs);

    public List<Entity> getAllMessages();

    public MessageService getMessageService();
}
