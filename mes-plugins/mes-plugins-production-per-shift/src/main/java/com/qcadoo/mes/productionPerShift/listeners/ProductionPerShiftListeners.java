package com.qcadoo.mes.productionPerShift.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductionPerShiftListeners {

    public void redirectToProductionPerShift(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId != null) {
            String url = "../page/productionPerShift/productionPerShift.html?context={\"form.id\":\"" + orderId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }
}
