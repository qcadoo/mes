/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.orders.states;

import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_FORM;
import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_GRID;
import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_STATE;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class OrderStatesService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    private void changeOrderStateTo(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final OrderStates oldState, final OrderStates newState) {

        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(FIELD_FORM);
        Entity order = form.getEntity();

        FieldComponent orderState = (FieldComponent) viewDefinitionState.getComponentByReference(FIELD_STATE);
        orderState.setFieldValue(newState.getStringValue());
        state.performEvent(viewDefinitionState, "save", new String[0]);

        Entity orderFromDB = order.getDataDefinition().get(order.getId());
        if (!orderFromDB.getStringField(FIELD_STATE).equals(newState.getStringValue())) {
            orderState.setFieldValue(oldState.getStringValue());
        }
    }

    public void changeOrderStateToAccepted(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        changeOrderStateTo(viewDefinitionState, state, OrderStates.PENDING, OrderStates.ACCEPTED);
    }

    public void changeOrderStateToInProgress(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(FIELD_FORM);
        Entity order = form.getEntity();
        if (OrderStates.ACCEPTED.getStringValue().equals(order.getStringField(FIELD_STATE))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.ACCEPTED, OrderStates.IN_PROGRESS);
        } else if (OrderStates.INTERRUPTED.getStringValue().equals(order.getStringField(FIELD_STATE))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.INTERRUPTED, OrderStates.IN_PROGRESS);
        }
    }

    public void changeOrderStateToCompleted(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        changeOrderStateTo(viewDefinitionState, state, OrderStates.IN_PROGRESS, OrderStates.COMPLETED);
    }

    public void changeOrderStateToDeclined(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(FIELD_FORM);
        Entity order = form.getEntity();
        if (OrderStates.ACCEPTED.getStringValue().equals(order.getStringField(FIELD_STATE))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.ACCEPTED, OrderStates.DECLINED);
        } else if (OrderStates.PENDING.getStringValue().equals(order.getStringField(FIELD_STATE))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.PENDING, OrderStates.DECLINED);
        }
    }

    public void changeOrderStateToAbandoned(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(FIELD_FORM);
        Entity order = form.getEntity();
        if (OrderStates.IN_PROGRESS.getStringValue().equals(order.getStringField(FIELD_STATE))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.IN_PROGRESS, OrderStates.ABANDONED);
        } else if (OrderStates.INTERRUPTED.getStringValue().equals(order.getStringField(FIELD_STATE))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.INTERRUPTED, OrderStates.ABANDONED);
        }
    }

    public void changeOrderStateToInterrupted(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        changeOrderStateTo(viewDefinitionState, state, OrderStates.IN_PROGRESS, OrderStates.INTERRUPTED);
    }

    private void changeOrderStateToForGrid(final ComponentState state, final Long id, final OrderStates newState) {
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
        order.setField(FIELD_STATE, newState.getStringValue());

        order.getDataDefinition().save(order);
        Entity orderFromDB = order.getDataDefinition().get(order.getId());
        if (!orderFromDB.getStringField(FIELD_STATE).equals(newState.getStringValue())) {
            StringBuilder errorMessages = new StringBuilder();
            for (ErrorMessage message : order.getErrors().values()) {
                errorMessages.append(translationService.translate(message.getMessage(), state.getLocale(), message.getVars()));
                errorMessages.append(", ");
            }
            state.addMessage("orders.order.orderStates.error", MessageType.FAILURE, false, orderFromDB.getStringField("name"),
                    errorMessages.toString());

        }

    }

    public void changeOrderStateToAcceptedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference(FIELD_GRID);
        for (Long id : grid.getSelectedEntitiesIds()) {
            changeOrderStateToForGrid(state, id, OrderStates.ACCEPTED);
        }
    }

    public void changeOrderStateToInProgressForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference(FIELD_GRID);
        for (Long id : grid.getSelectedEntitiesIds()) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
            if (OrderStates.ACCEPTED.getStringValue().equals(order.getStringField(FIELD_STATE))) {
                changeOrderStateToForGrid(state, id, OrderStates.IN_PROGRESS);
            } else if (OrderStates.INTERRUPTED.getStringValue().equals(order.getStringField(FIELD_STATE))) {
                changeOrderStateToForGrid(state, id, OrderStates.IN_PROGRESS);
            }
        }
    }

    public void changeOrderStateToCompletedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference(FIELD_GRID);
        for (Long id : grid.getSelectedEntitiesIds()) {
            changeOrderStateToForGrid(state, id, OrderStates.COMPLETED);
        }
    }

    public void changeOrderStateToDeclinedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference(FIELD_GRID);
        for (Long id : grid.getSelectedEntitiesIds()) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
            if (OrderStates.ACCEPTED.getStringValue().equals(order.getStringField(FIELD_STATE))) {
                changeOrderStateToForGrid(state, id, OrderStates.DECLINED);
            } else if (OrderStates.PENDING.getStringValue().equals(order.getStringField(FIELD_STATE))) {
                changeOrderStateToForGrid(state, id, OrderStates.DECLINED);
            }
        }
    }

    public void changeOrderStateToAbandonedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference(FIELD_GRID);
        for (Long id : grid.getSelectedEntitiesIds()) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
            if (OrderStates.IN_PROGRESS.getStringValue().equals(order.getStringField(FIELD_STATE))) {
                changeOrderStateToForGrid(state, id, OrderStates.ABANDONED);
            } else if (OrderStates.INTERRUPTED.getStringValue().equals(order.getStringField(FIELD_STATE))) {
                changeOrderStateToForGrid(state, id, OrderStates.ABANDONED);
            }
        }
    }

    public void changeOrderStateToInterruptedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference(FIELD_GRID);
        for (Long id : grid.getSelectedEntitiesIds()) {
            changeOrderStateToForGrid(state, id, OrderStates.INTERRUPTED);
        }
    }

    public void setFieldsRequired(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(FIELD_FORM);
        if (form.getEntityId() == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                form.getEntityId());
        if (order != null) {
            if (order.getStringField(FIELD_STATE).equals(OrderStates.ACCEPTED.getStringValue())
                    || order.getStringField(FIELD_STATE).equals(OrderStates.IN_PROGRESS.getStringValue())
                    || order.getStringField(FIELD_STATE).equals(OrderStates.INTERRUPTED.getStringValue())) {
                for (String reference : Arrays.asList("dateTo", "dateFrom", "defaultTechnology", "technology")) {
                    FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
                    field.setRequired(true);
                }
            } else if (order.getStringField(FIELD_STATE).equals(OrderStates.COMPLETED.getStringValue())) {
                for (String reference : Arrays.asList("dateTo", "dateFrom", "defaultTechnology", "technology", "doneQuantity")) {
                    FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
                    field.setRequired(true);
                }
            }
        }
    }

}
