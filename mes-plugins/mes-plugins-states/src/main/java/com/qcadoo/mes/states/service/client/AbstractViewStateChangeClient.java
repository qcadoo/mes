package com.qcadoo.mes.states.service.client;

import static com.qcadoo.mes.states.messages.constants.MessageType.FAILURE;
import static com.qcadoo.mes.states.messages.constants.MessageType.INFO;
import static com.qcadoo.mes.states.messages.constants.MessageType.SUCCESS;
import static com.qcadoo.mes.states.messages.constants.MessageType.parseString;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.splitArgs;

import java.util.List;

import com.qcadoo.mes.states.messages.constants.MessageFields;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public abstract class AbstractViewStateChangeClient implements ViewStateChangeClient {

    @Override
    public void changeState(final ViewDefinitionState view, final ComponentState component, final Entity entity,
            final String targetState) {
        Entity stateChangeEntity = getStateChangeService().createNewStateChangeEntity(entity, targetState);
        getStateChangeService().changeState(stateChangeEntity);
        addStateMessagesToView(component, stateChangeEntity);
    }

    protected abstract StateChangeService getStateChangeService();

    protected void addStateMessagesToView(final ComponentState component, final Entity stateChangeEntity) {
        List<Entity> stateMessages = stateChangeEntity.getHasManyField("messages");
        for (Entity stateMessage : stateMessages) {
            addStateMessageToComponent(component, stateMessage);
        }
    }

    protected void addStateMessageToComponent(final ComponentState component, final Entity stateMessage) {
        final MessageType stateMsgType = parseString(stateMessage.getStringField(MessageFields.TYPE));
        final com.qcadoo.view.api.ComponentState.MessageType viewMsgType = getViewMessageType(stateMsgType);
        final String translationKey = stateMessage.getStringField(MessageFields.TRANSLATION_KEY);
        final String[] translationArgs = splitArgs(stateMessage.getStringField(MessageFields.TRANSLATION_ARGS));
        component.addMessage(translationKey, viewMsgType, translationArgs);
    }

    protected com.qcadoo.view.api.ComponentState.MessageType getViewMessageType(final MessageType type) {
        if (SUCCESS.equals(type)) {
            return com.qcadoo.view.api.ComponentState.MessageType.SUCCESS;
        } else if (INFO.equals(type)) {
            return com.qcadoo.view.api.ComponentState.MessageType.INFO;
        } else if (FAILURE.equals(type)) {
            return com.qcadoo.view.api.ComponentState.MessageType.FAILURE;
        } else {
            return com.qcadoo.view.api.ComponentState.MessageType.INFO;
        }
    }
}
