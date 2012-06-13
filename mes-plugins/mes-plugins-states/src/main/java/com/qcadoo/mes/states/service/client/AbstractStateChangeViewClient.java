package com.qcadoo.mes.states.service.client;

import static com.qcadoo.mes.states.constants.StateChangeStatus.PAUSED;
import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.exception.AnotherChangeInProgressException;
import com.qcadoo.mes.states.exception.StateChangeException;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Configurable
public abstract class AbstractStateChangeViewClient implements StateChangeViewClient {

    @Autowired
    private StateChangeViewClientUtil viewClientUtil;

    @Autowired
    private StateChangeViewClientValidationUtil viewClientValidationUtil;

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
        final List<Entity> entities = viewClientUtil.getEntitiesFromComponent(viewContext);
        for (Entity entity : entities) {
            changeState(viewContext, targetState, entity);
        }
    }

    @Override
    public final void changeState(final ViewContextHolder viewContext, final String targetState, final Entity entity) {
        try {
            final Entity stateChangeEntity = getStateChangeService().createNewStateChangeEntity(entity, targetState);
            getStateChangeService().changeState(stateChangeEntity);
            viewClientUtil.refreshComponent(viewContext);
            showMessages(viewContext, stateChangeEntity);
        } catch (AnotherChangeInProgressException e) {
            viewContext.getMessagesConsumer().addMessage("states.messages.change.failure.anotherChangeInProgress",
                    com.qcadoo.view.api.ComponentState.MessageType.FAILURE);
        } catch (Exception e) {
            throw new StateChangeException(e);
        }
    }

    private void showMessages(final ViewContextHolder viewContext, final Entity stateChangeEntity) {
        viewClientUtil.addStateMessagesToView(viewContext.getMessagesConsumer(), stateChangeEntity);
        final String messagesFieldName = getStateChangeService().getChangeEntityDescriber().getMessagesFieldName();
        final String ownerFieldName = getStateChangeService().getChangeEntityDescriber().getOwnerFieldName();
        viewClientValidationUtil.addValidationErrorMessages(viewContext.getMessagesConsumer(),
                stateChangeEntity.getBelongsToField(ownerFieldName), stateChangeEntity.getHasManyField(messagesFieldName));
        addFinalMessage(viewContext.getMessagesConsumer(), stateChangeEntity);
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

    protected final Object parseStateEnum(final String stateString) {
        return getStateChangeService().getChangeEntityDescriber().parseStateEnum(stateString);
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

}
