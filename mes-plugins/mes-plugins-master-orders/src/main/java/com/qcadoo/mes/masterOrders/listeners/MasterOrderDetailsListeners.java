package com.qcadoo.mes.masterOrders.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.hooks.MasterOrderDetailsHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class MasterOrderDetailsListeners {

    @Autowired
    private MasterOrderDetailsHooks masterOrderDetailsHooks;

    public void hideFieldDependOnMasterOrderType(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        masterOrderDetailsHooks.hideFieldDependOnMasterOrderType(view);
    }
}
