package com.qcadoo.mes.orders.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderStatesViewService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OrderStateChangingService orderStateChangingService;

    public void toAccepted(final DataDefinition dd, final Entity order) {
        if (!("02accepted".equals(order.getStringField("state")))) {
            return;
        }
        if (order.getId() == null) {
            return;
        }
        Entity oldOrder = dd.get(order.getId());
        String oldState = oldOrder.getStringField("state");
        if (!("01pending".equals(oldState))) {
            return;
        }
        orderStateChangingService.saveLogging(order, "01pending", "02accepted");
    }

    public void changeOrderStateToAccepted(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        changeOrderStateTo(viewDefinitionState, state, OrderStates.ACCEPTED, OrderStates.PENDING);
    }

    private void changeOrderStateTo(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final OrderStates newState, final OrderStates oldState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity();
        order.setField("state", newState.getStringValue());
        order.getDataDefinition().save(order);
        orderStateChangingService.saveLogging(order, oldState.getStringValue(), newState.getStringValue());
    }

    public void changeOrderStateToInProgress(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity();
        if (!("02accepted".equals(order.getStringField("state"))) && !("06interrupted".equals(order.getStringField("state")))) {
            return;
        }
        order.setField("state", "03inProgress");
        order.getDataDefinition().save(order);
        orderStateChangingService.saveLogging(order, order.getStringField("state"), "03inProgress");
    }

    public void changeOrderStateToCompleted(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity();
        if (!("03inProgress".equals(order.getStringField("state")))) {
            return;
        }
        order.setField("state", "04completed");
        order.getDataDefinition().save(order);
        orderStateChangingService.saveLogging(order, order.getStringField("state"), "04completed");
    }

    public void changeOrderStateToDeclined(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity();
        if (!("02accepted".equals(order.getStringField("state"))) && !("01pending".equals(order.getStringField("state")))) {
            return;
        }
        order.setField("state", "05declined");
        order.getDataDefinition().save(order);
        orderStateChangingService.saveLogging(order, order.getStringField("state"), "05declined");
    }

    public void changeOrderStateToAbandoned(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity();
        if (!("03inProgress".equals(order.getStringField("state"))) && !("06interrupted".equals(order.getStringField("state")))) {
            return;
        }
        order.setField("state", "07abandoned");
        order.getDataDefinition().save(order);
        orderStateChangingService.saveLogging(order, order.getStringField("state"), "07abandoned");
    }

    public void changeOrderStateToInterrupted(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity();
        if (!("03inProgress".equals(order.getStringField("state")))) {
            return;
        }
        order.setField("state", "06interrupted");
        order.getDataDefinition().save(order);
        orderStateChangingService.saveLogging(order, order.getStringField("state"), "06interrupted");
    }

}
