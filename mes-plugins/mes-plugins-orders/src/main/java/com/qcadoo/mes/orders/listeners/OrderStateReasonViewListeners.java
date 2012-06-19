package com.qcadoo.mes.orders.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.client.OrderStateChangeViewClient;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderStateReasonViewListeners {

    @Autowired
    private OrderStateChangeAspect orderStateChangeService;

    @Autowired
    private OrderStateChangeViewClient orderStateChangeViewClient;

    public void continueStateChange(final ViewDefinitionState view, final ComponentState form, final String[] args) {
        final StateChangeContext stateContext = getStateChangeContext((FormComponent) form);
        stateContext.setStatus(StateChangeStatus.IN_PROGRESS);
        orderStateChangeService.changeState(stateContext);

        final ViewContextHolder viewContext = new ViewContextHolder(view, form);
        orderStateChangeViewClient.showMessages(viewContext, stateContext);
    }

    public void cancelStateChange(final ViewDefinitionState view, final ComponentState form, final String[] args) {
        final StateChangeContext stateContext = getStateChangeContext((FormComponent) form);
        stateContext.setStatus(StateChangeStatus.CANCELED);
        stateContext.save();

        final ViewContextHolder viewContext = new ViewContextHolder(view, form);
        orderStateChangeViewClient.showMessages(viewContext, stateContext);
    }

    private StateChangeContext getStateChangeContext(final FormComponent form) {
        final Entity stateChangeEntity = ((FormComponent) form).getEntity();
        return orderStateChangeService.buildStateChangeContext(stateChangeEntity);
    }

}
