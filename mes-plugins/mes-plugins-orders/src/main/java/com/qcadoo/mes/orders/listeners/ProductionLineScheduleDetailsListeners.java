package com.qcadoo.mes.orders.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.states.ProductionLineScheduleServiceMarker;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductionLineScheduleDetailsListeners {

    @Autowired
    private StateExecutorService stateExecutorService;

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        stateExecutorService.changeState(ProductionLineScheduleServiceMarker.class, view, args);
    }

}
