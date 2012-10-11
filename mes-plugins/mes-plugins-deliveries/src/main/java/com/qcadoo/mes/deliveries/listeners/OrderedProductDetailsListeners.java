package com.qcadoo.mes.deliveries.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.hooks.OrderedProductDetailsHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class OrderedProductDetailsListeners {

    @Autowired
    private OrderedProductDetailsHooks orderedProductDetailsHooks;

    public void fillUnitsFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        orderedProductDetailsHooks.fillUnitsFields(view);
    }
}
