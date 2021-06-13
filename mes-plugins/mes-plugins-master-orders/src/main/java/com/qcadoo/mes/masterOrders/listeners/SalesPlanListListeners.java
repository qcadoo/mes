package com.qcadoo.mes.masterOrders.listeners;

import com.qcadoo.mes.masterOrders.states.SalesPlanServiceMarker;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SalesPlanListListeners {

    @Autowired
    private StateExecutorService stateExecutorService;

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        stateExecutorService.changeState(SalesPlanServiceMarker.class, view, args);
    }
}
