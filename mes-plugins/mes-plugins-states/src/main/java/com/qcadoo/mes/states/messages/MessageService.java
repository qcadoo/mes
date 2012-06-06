package com.qcadoo.mes.states.messages;

import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.model.api.Entity;

public interface MessageService {

    Entity createMessage(final MessageType type, final String translationKey, final String... translationArgs);
}
