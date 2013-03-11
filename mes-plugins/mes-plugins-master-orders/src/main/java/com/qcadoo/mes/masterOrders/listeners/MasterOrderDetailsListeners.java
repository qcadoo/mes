package com.qcadoo.mes.masterOrders.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.hooks.MasterOrderDetailsHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class MasterOrderDetailsListeners {

    @Autowired
    private MasterOrderDetailsHooks masterOrderDetailsHooks;

    public void hideFieldDependOnMasterOrderType(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        masterOrderDetailsHooks.hideFieldDependOnMasterOrderType(view);
    }

    public void fillUnitField(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        masterOrderDetailsHooks.fillUnitField(view);
    }

    public void fillDefaultTechnology(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        masterOrderDetailsHooks.fillDefaultTechnology(view);
    }

    public void refreshView(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        form.performEvent(view, "refresh");
    }

}
