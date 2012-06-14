package com.qcadoo.mes.states.messages;

import static com.qcadoo.mes.states.messages.constants.StateMessageType.VALIDATION_ERROR;

import java.util.List;

import com.google.common.collect.Lists;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.Entity;

public class MessagesHolderImpl implements MessagesHolder {

    private final MessageService messageService;

    private final List<Entity> messagesList;

    public MessagesHolderImpl(final MessageService messageService) {
        this.messageService = messageService;
        this.messagesList = Lists.newArrayList();
    }

    @Override
    public void addFieldMessage(final String translationKey, final StateMessageType type, final String fieldName,
            final String... translationArgs) {
        messagesList.add(messageService.createMessage(translationKey, type, fieldName, translationArgs));
    }

    @Override
    public void addMessage(final String translationKey, final StateMessageType type, final String... translationArgs) {
        messagesList.add(messageService.createMessage(translationKey, type, null, translationArgs));
    }

    @Override
    public void addFieldValidationError(final String translationKey, final String fieldName, final String... translationArgs) {
        messagesList.add(messageService.createMessage(translationKey, VALIDATION_ERROR, fieldName, translationArgs));
    }

    @Override
    public void addValidationError(final String translationKey, final String... translationArgs) {
        messagesList.add(messageService.createMessage(translationKey, VALIDATION_ERROR, null, translationArgs));
    }

    @Override
    public List<Entity> getAllMessages() {
        return messagesList;
    }

    @Override
    public MessageService getMessageService() {
        return messageService;
    }

}
