package com.qcadoo.mes.deliveries.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.hooks.DeliveredProductDetailsHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class DeliveredProductDetailsListeners {

    private DeliveredProductDetailsHooks deliveredProductDetailsHooks;

    public void fillOrderedQuantities(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveredProductDetailsHooks.fillOrderedQuantities(view);
    }

    public void fillUnitsFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveredProductDetailsHooks.fillUnitsFields(view);
    }

}
