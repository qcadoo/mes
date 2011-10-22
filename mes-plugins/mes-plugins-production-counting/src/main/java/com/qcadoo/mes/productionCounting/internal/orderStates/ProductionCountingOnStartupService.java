package com.qcadoo.mes.productionCounting.internal.orderStates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.orders.states.OrderStatesChangingService;
import com.qcadoo.plugin.api.Module;

@Component
public class ProductionCountingOnStartupService extends Module {

    @Autowired
    private OrderStatesChangingService orderStatesChangingService;

    @Autowired
    private ProductionCountingOrderStatesListener productionCountingOrderStatesListener;

    @Override
    public void enableOnStartup() {
        orderStatesChangingService.addOrderStateListener(productionCountingOrderStatesListener);
    }

    @Override
    public void disableOnStartup() {
        orderStatesChangingService.removeOrderStateListener(productionCountingOrderStatesListener);
    }
}
