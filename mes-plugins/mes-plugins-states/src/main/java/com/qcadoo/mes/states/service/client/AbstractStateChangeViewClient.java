package com.qcadoo.mes.states.service.client;

import static com.qcadoo.mes.states.constants.StateChangeStatus.PAUSED;
import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;
import static com.qcadoo.mes.states.messages.constants.MessageType.parseString;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.splitArgs;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.exception.AnotherChangeInProgressException;
import com.qcadoo.mes.states.exception.StateChangeException;
import com.qcadoo.mes.states.messages.constants.MessageFields;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

public abstract class AbstractStateChangeViewClient implements StateChangeViewClient {

    protected abstract StateChangeService getStateChangeService();

    @Override
    public final void changeState(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        final String newStateString = args[0];
        Preconditions.checkNotNull(newStateString, "Missing target state argument!");
        Preconditions.checkArgument(parseStateEnum(newStateString) != null, "Unsupported state value: " + newStateString);
        changeState(buildViewContext(view, component), newStateString);
    }

    @Override
    public final void changeState(final ViewContextHolder viewContext, final String targetState) {
        final List<Entity> entities = getEntitiesFromComponent(viewContext);
        for (Entity entity : entities) {
            changeState(viewContext, targetState, entity);
        }
    }

    @Override
    public final void changeState(final ViewContextHolder viewContext, final String targetState, final Entity entity) {
        try {
            final Entity stateChangeEntity = getStateChangeService().createNewStateChangeEntity(entity, targetState);
            getStateChangeService().changeState(stateChangeEntity);
            refreshComponent(viewContext);
            addStateMessagesToView(viewContext.getMessagesConsumer(), stateChangeEntity);
            addFinalMessage(viewContext.getMessagesConsumer(), stateChangeEntity);
        } catch (AnotherChangeInProgressException e) {
            viewContext.getMessagesConsumer().addMessage("states.messages.change.failure.anotherChangeInProgress",
                    com.qcadoo.view.api.ComponentState.MessageType.FAILURE);
        } catch (Exception e) {
            throw new StateChangeException(e);
        }
    }

    private ViewContextHolder buildViewContext(final ViewDefinitionState view, final ComponentState component) {
        if (component instanceof FormComponent) {
            return new ViewContextHolder(view, component);
        } else if (component instanceof GridComponent) {
            return getViewContextHolderForListView(view, component);
        } else {
            throw new IllegalArgumentException("Unsupported view component " + component);
        }
    }

    /**
     * Returns {@link ViewContextHolder} for list (grid) view. Default messageConsumer is set to component which invoke event
     * (usually grid component). Override this method if you want to change default consumer.
     * 
     * @param view
     * @param component
     * @return
     */
    protected ViewContextHolder getViewContextHolderForListView(final ViewDefinitionState view, final ComponentState component) {
        return new ViewContextHolder(view, component);
    }

    private List<Entity> getEntitiesFromComponent(final ViewContextHolder viewContext) {
        final List<Entity> entities = Lists.newArrayList();

        if (viewContext.getInvoker() instanceof FormComponent && isFormEntityValid(viewContext)) {
            entities.add(((FormComponent) viewContext.getInvoker()).getEntity());
        } else if (viewContext.getInvoker() instanceof GridComponent) {
            entities.addAll(((GridComponent) viewContext.getInvoker()).getSelectedEntities());
        } else {
            throw new IllegalArgumentException("Unsupported view component " + viewContext.getInvoker());
        }

        return entities;
    }

    protected final Object parseStateEnum(final String stateString) {
        return getStateChangeService().getChangeEntityDescriber().parseStateEnum(stateString);
    }

    private boolean isFormEntityValid(final ViewContextHolder viewContext) {
        final FormComponent formComponent = (FormComponent) viewContext.getInvoker();
        formComponent.performEvent(viewContext.getViewDefinitionState(), "save", new String[0]);
        return formComponent.isValid();
    }

    protected final void refreshComponent(final ViewContextHolder viewContext) {
        if (viewContext.getInvoker() instanceof FormComponent) {
            viewContext.getInvoker().performEvent(viewContext.getViewDefinitionState(), "reset", new String[0]);
        }
    }

    private void addFinalMessage(final ComponentState component, final Entity stateChange) {
        final StateChangeEntityDescriber describer = getStateChangeService().getChangeEntityDescriber();
        final String statusStringValue = stateChange.getStringField(describer.getStatusFieldName());
        final StateChangeStatus status = StateChangeStatus.parseString(statusStringValue);
        if (SUCCESSFUL.equals(status)) {
            component.addMessage("states.messages.change.successful", com.qcadoo.view.api.ComponentState.MessageType.SUCCESS);
        } else if (PAUSED.equals(status)) {
            component.addMessage("states.messages.change.paused", com.qcadoo.view.api.ComponentState.MessageType.INFO);
        }
    }

    protected final void addStateMessagesToView(final ComponentState component, final Entity stateChangeEntity) {
        final List<Entity> stateMessages = stateChangeEntity.getHasManyField("messages");
        for (Entity stateMessage : stateMessages) {
            addStateMessageToComponent(component, stateMessage);
        }
    }

    protected final void addStateMessageToComponent(final ComponentState component, final Entity stateMessage) {
        final MessageType stateMsgType = parseString(stateMessage.getStringField(MessageFields.TYPE));
        final com.qcadoo.view.api.ComponentState.MessageType viewMsgType = convertViewMessageType(stateMsgType);
        final String translationKey = stateMessage.getStringField(MessageFields.TRANSLATION_KEY);
        final String[] translationArgs = splitArgs(stateMessage.getStringField(MessageFields.TRANSLATION_ARGS));
        component.addMessage(translationKey, viewMsgType, translationArgs);
    }

    /**
     * Convert {@link MessageType} to appropriate {@link com.qcadoo.view.api.ComponentState.MessageType}
     * 
     * @param type
     *            {@link MessageType}
     * @return appropriate {@link com.qcadoo.view.api.ComponentState.MessageType}
     */
    protected final com.qcadoo.view.api.ComponentState.MessageType convertViewMessageType(final MessageType type) {
        switch (type) {
            case SUCCESS:
                return com.qcadoo.view.api.ComponentState.MessageType.SUCCESS;
            case INFO:
                return com.qcadoo.view.api.ComponentState.MessageType.INFO;
            case FAILURE:
                return com.qcadoo.view.api.ComponentState.MessageType.FAILURE;
            default:
                return com.qcadoo.view.api.ComponentState.MessageType.INFO;
        }
    }
}
