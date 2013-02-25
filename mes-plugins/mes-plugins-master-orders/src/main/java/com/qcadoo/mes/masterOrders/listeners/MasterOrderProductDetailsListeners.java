package com.qcadoo.mes.masterOrders.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.hooks.MasterOrderProductDetailsHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class MasterOrderProductDetailsListeners {

    @Autowired
    private MasterOrderProductDetailsHooks masterOrderProductDetailsHooks;

    public void fillUnitField(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        masterOrderProductDetailsHooks.fillUnitField(view);
    }

}
