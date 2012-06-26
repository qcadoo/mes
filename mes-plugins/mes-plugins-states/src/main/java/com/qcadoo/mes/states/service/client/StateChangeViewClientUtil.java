package com.qcadoo.mes.states.service.client;

import static com.qcadoo.mes.states.messages.constants.StateMessageType.VALIDATION_ERROR;
import static com.qcadoo.mes.states.messages.constants.StateMessageType.parseString;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.convertViewMessageType;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.getArgs;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.getKey;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.isAutoClosed;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.states.messages.MessagesHolder;
import com.qcadoo.mes.states.messages.constants.MessageFields;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class StateChangeViewClientUtil {

    public List<Entity> getEntitiesFromComponent(final ViewContextHolder viewContext) {
        final List<Entity> entities = Lists.newArrayList();

        if (viewContext.getInvoker() instanceof FormComponent) {
            if (isFormEntityValid(viewContext)) {
                entities.add(((FormComponent) viewContext.getInvoker()).getEntity());
            }
        } else if (viewContext.getInvoker() instanceof GridComponent) {
            entities.addAll(((GridComponent) viewContext.getInvoker()).getSelectedEntities());
        } else {
            throw new IllegalArgumentException("Unsupported view component " + viewContext.getInvoker());
        }

        return entities;
    }

    public boolean isFormEntityValid(final ViewContextHolder viewContext) {
        final FormComponent formComponent = (FormComponent) viewContext.getInvoker();
        final Entity entity = formComponent.getEntity();
        formComponent.setEntity(entity.getDataDefinition().save(entity));
        return formComponent.isValid();
    }

    public void refreshComponent(final ViewContextHolder viewContext) {
        if (viewContext.getInvoker() instanceof FormComponent) {
            viewContext.getInvoker().performEvent(viewContext.getViewDefinitionState(), "reset", new String[0]);
        }
    }

    public void addStateMessagesToView(final ComponentState component, final MessagesHolder messageHolder) {
        for (Entity stateMessage : messageHolder.getAllMessages()) {
            addStateMessageToComponent(component, stateMessage);
        }
    }

    public void addStateMessageToComponent(final ComponentState component, final Entity stateMessage) {
        final StateMessageType stateMsgType = parseString(stateMessage.getStringField(MessageFields.TYPE));
        if (VALIDATION_ERROR.equals(stateMsgType)) {
            return;
        }
        component.addMessage(getKey(stateMessage), convertViewMessageType(stateMsgType), isAutoClosed(stateMessage),
                getArgs(stateMessage));
    }

}
