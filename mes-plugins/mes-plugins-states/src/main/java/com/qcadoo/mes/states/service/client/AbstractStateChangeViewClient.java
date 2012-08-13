/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.states.service.client;

import static com.qcadoo.mes.states.constants.StateChangeStatus.CANCELED;
import static com.qcadoo.mes.states.constants.StateChangeStatus.FAILURE;
import static com.qcadoo.mes.states.constants.StateChangeStatus.PAUSED;
import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.hasFailureMessages;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.exception.AnotherChangeInProgressException;
import com.qcadoo.mes.states.exception.StateChangeException;
import com.qcadoo.mes.states.exception.StateTransitionNotAlloweException;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

public abstract class AbstractStateChangeViewClient implements StateChangeViewClient {

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

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
            final StateChangeContext stateChangeContext = stateChangeContextBuilder.build(getStateChangeService()
                    .getChangeEntityDescriber(), entity, targetState);
            getStateChangeService().changeState(stateChangeContext);
            viewClientUtil.refreshComponent(viewContext);
            showMessages(viewContext, stateChangeContext);
        } catch (AnotherChangeInProgressException e) {
            viewContext.getMessagesConsumer().addMessage("states.messages.change.failure.anotherChangeInProgress",
                    MessageType.FAILURE);
        } catch (StateTransitionNotAlloweException e) {
            viewContext.getMessagesConsumer().addMessage("states.messages.change.failure.transitionNotAllowed",
                    MessageType.FAILURE);
        } catch (Exception e) {
            throw new StateChangeException(e);
        }
    }

    @Override
    public void showMessages(final ViewContextHolder viewContext, final StateChangeContext stateChangeContext) {
        viewClientUtil.addStateMessagesToView(viewContext.getMessagesConsumer(), stateChangeContext);
        viewClientValidationUtil.addValidationErrorMessages(viewContext.getMessagesConsumer(), stateChangeContext);
        addFinalMessage(viewContext.getMessagesConsumer(), stateChangeContext);
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

    private void addFinalMessage(final ComponentState component, final StateChangeContext stateChangeContext) {
        final StateChangeStatus status = stateChangeContext.getStatus();

        if (SUCCESSFUL.equals(status)) {
            component.addMessage("states.messages.change.successful", MessageType.SUCCESS);
        } else if (PAUSED.equals(status)) {
            component.addMessage("states.messages.change.paused", MessageType.INFO);
        } else if (FAILURE.equals(status) && !hasFailureMessages(stateChangeContext.getAllMessages())) {
            component.addMessage("states.messages.change.failure", MessageType.FAILURE);
        } else if (CANCELED.equals(status)) {
            component.addMessage("states.messages.change.canceled", MessageType.INFO);
        }
    }

}
