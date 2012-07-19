package com.qcadoo.mes.avgLaborCostCalcForOrder.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class HourlyCostNormsInOrderDetailsALHCListeners {

    public final void showCalculateForWorkerOnLine(final ViewDefinitionState viewState, final ComponentState triggerState,
            final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId == null) {
            return;
        }

        String url = "../page/avgLaborCostCalcForOrder/avgLaborCostCalcForOrderDetails.html?context={\"orderId\":\"" + orderId
                + "\"}";
        viewState.redirectTo(url, false, true);
    }
}
