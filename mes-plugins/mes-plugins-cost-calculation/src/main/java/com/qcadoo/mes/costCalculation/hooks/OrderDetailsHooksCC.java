package com.qcadoo.mes.costCalculation.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.util.OrderDetailsRibbonHelper;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class OrderDetailsHooksCC {

    @Autowired
    private OrderDetailsRibbonHelper orderDetailsRibbonHelper;

    public void onBeforeRender(final ViewDefinitionState view) {
        orderDetailsRibbonHelper.setButtonEnabledForPendingOrder(view, "costCalculate", "costCalculate",
                OrderDetailsRibbonHelper.HAS_CHECKED_OR_ACCEPTED_TECHNOLOGY);
    }

}
