package com.qcadoo.mes.orders.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class OrderStatesViewService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OrderStateChangingService orderStateChangingService;

    @Autowired
    private PluginAccessor pluginAccessor;

    private void changeOrderStateTo(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final OrderStates oldState, final OrderStates newState) {
        FieldComponent orderState = (FieldComponent) viewDefinitionState.getComponentByReference("state");
        orderState.setFieldValue(newState.getStringValue());

        FieldComponent externalSynchronized = (FieldComponent) viewDefinitionState
                .getComponentByReference("externalSynchronized");
        FieldComponent externalNumber = (FieldComponent) viewDefinitionState.getComponentByReference("externalNumber");

        if (hasIntegrationWithExternalSystem() && StringUtils.hasText((String) externalNumber.getFieldValue())) {
            externalSynchronized.setFieldValue("false");
        } else {
            externalSynchronized.setFieldValue("true");
        }

        state.performEvent(viewDefinitionState, "save", new String[0]);

    }

    public void changeOrderStateToAccepted(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        changeOrderStateTo(viewDefinitionState, state, OrderStates.PENDING, OrderStates.ACCEPTED);
    }

    public void changeOrderStateToInProgress(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity();
        if ((OrderStates.ACCEPTED.getStringValue().equals(order.getStringField("state")))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.ACCEPTED, OrderStates.IN_PROGRESS);
        } else if (OrderStates.INTERRUPTED.getStringValue().equals(order.getStringField("state"))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.INTERRUPTED, OrderStates.IN_PROGRESS);
        }
    }

    public void changeOrderStateToCompleted(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        changeOrderStateTo(viewDefinitionState, state, OrderStates.IN_PROGRESS, OrderStates.COMPLETED);
    }

    public void changeOrderStateToDeclined(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity();
        if ((OrderStates.ACCEPTED.getStringValue().equals(order.getStringField("state")))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.ACCEPTED, OrderStates.DECLINED);
        } else if (OrderStates.PENDING.getStringValue().equals(order.getStringField("state"))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.PENDING, OrderStates.DECLINED);
        }
    }

    public void changeOrderStateToAbandoned(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity();
        if ((OrderStates.IN_PROGRESS.getStringValue().equals(order.getStringField("state")))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.IN_PROGRESS, OrderStates.ABANDONED);
        } else if (OrderStates.INTERRUPTED.getStringValue().equals(order.getStringField("state"))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.INTERRUPTED, OrderStates.ABANDONED);
        }
    }

    public void changeOrderStateToInterrupted(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        changeOrderStateTo(viewDefinitionState, state, OrderStates.IN_PROGRESS, OrderStates.INTERRUPTED);
    }

    private void changeOrderStateToForGrid(final Long id, final OrderStates oldState, final OrderStates newState) {
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
        order.setField("state", newState.getStringValue());
        order.getDataDefinition().save(order);
        orderStateChangingService.saveLogging(order, oldState.getStringValue(), newState.getStringValue());
    }

    public void changeOrderStateToAcceptedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");
        for (Long id : grid.getSelectedEntitiesIds()) {
            changeOrderStateToForGrid(id, OrderStates.PENDING, OrderStates.ACCEPTED);
        }
    }

    public void changeOrderStateToInProgressForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");
        for (Long id : grid.getSelectedEntitiesIds()) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
            if ((OrderStates.ACCEPTED.getStringValue().equals(order.getStringField("state")))) {
                changeOrderStateToForGrid(id, OrderStates.ACCEPTED, OrderStates.IN_PROGRESS);
            } else if (OrderStates.INTERRUPTED.getStringValue().equals(order.getStringField("state"))) {
                changeOrderStateToForGrid(id, OrderStates.INTERRUPTED, OrderStates.IN_PROGRESS);
            }
        }
    }

    public void changeOrderStateToCompletedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");
        for (Long id : grid.getSelectedEntitiesIds()) {
            changeOrderStateToForGrid(id, OrderStates.IN_PROGRESS, OrderStates.COMPLETED);
        }
    }

    public void changeOrderStateToDeclinedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");
        for (Long id : grid.getSelectedEntitiesIds()) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
            if ((OrderStates.ACCEPTED.getStringValue().equals(order.getStringField("state")))) {
                changeOrderStateToForGrid(id, OrderStates.ACCEPTED, OrderStates.DECLINED);
            } else if (OrderStates.PENDING.getStringValue().equals(order.getStringField("state"))) {
                changeOrderStateToForGrid(id, OrderStates.PENDING, OrderStates.DECLINED);
            }
        }
    }

    public void changeOrderStateToAbandonedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");
        for (Long id : grid.getSelectedEntitiesIds()) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
            if ((OrderStates.IN_PROGRESS.getStringValue().equals(order.getStringField("state")))) {
                changeOrderStateToForGrid(id, OrderStates.IN_PROGRESS, OrderStates.ABANDONED);
            } else if (OrderStates.INTERRUPTED.getStringValue().equals(order.getStringField("state"))) {
                changeOrderStateToForGrid(id, OrderStates.INTERRUPTED, OrderStates.ABANDONED);
            }
        }
    }

    public void changeOrderStateToInterruptedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");
        for (Long id : grid.getSelectedEntitiesIds()) {
            changeOrderStateToForGrid(id, OrderStates.IN_PROGRESS, OrderStates.INTERRUPTED);
        }
    }

    private boolean hasIntegrationWithExternalSystem() {
        Plugin plugin = pluginAccessor.getPlugin("mesPluginsIntegrationErp");
        return plugin != null && plugin.getState().equals(PluginState.ENABLED);
    }
}
