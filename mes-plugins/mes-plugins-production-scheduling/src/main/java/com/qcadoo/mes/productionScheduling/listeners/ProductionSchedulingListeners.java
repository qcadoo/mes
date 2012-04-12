package com.qcadoo.mes.productionScheduling.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductionSchedulingListeners {

    public void redirectToOperationDurationDetails(final ViewDefinitionState viewDefinitionState,
            final ComponentState triggerState, final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId != null) {
            String url = "../page/productionScheduling/operationDurationDetails.html?context={\"form.id\":\"" + orderId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }

    }

}
