package com.qcadoo.mes.catNumbersInDeliveries.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.catNumbersInDeliveries.hooks.OrderedProductDetailsHooksCNID;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class OrderedProductListenersHooksCNID {

    @Autowired
    private OrderedProductDetailsHooksCNID detailsHooksCNID;

    public void setCatalogProductNumber(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        detailsHooksCNID.setCatalogProductNumber(view);
    }
}
