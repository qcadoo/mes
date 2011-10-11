package com.qcadoo.mes.orders.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderStatesViewService {

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

    private boolean hasIntegrationWithExternalSystem() {
        Plugin plugin = pluginAccessor.getPlugin("mesPluginsIntegrationErp");
        return plugin != null && plugin.getState().equals(PluginState.ENABLED);
    }
}
