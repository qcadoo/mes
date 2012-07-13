package com.qcadoo.mes.costNormsForOperationInOrder.listeners;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class HourlyCostNormsInOrderListeners {

    public final void showHourlyCostNorms(final ViewDefinitionState viewState, final ComponentState triggerState,
            final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("order.id", orderId);

        String url = "../page/costNormsForOperationInOrder/hourlyCostNormsInOrderDetails.html";
        viewState.redirectTo(url, false, true, parameters);
    }
}
