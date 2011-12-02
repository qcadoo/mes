package com.qcadoo.mes.basicProductionCounting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.orders.states.OrderStatesChangingService;
import com.qcadoo.plugin.api.Module;

@Component
public class BasicProductionCountingOnStartupService extends Module {

    @Autowired
    private OrderStatesChangingService orderChangingService;

    @Autowired
    private BasicProductionCountingOrderStatesListener orderStatesListener;

    @Override
    public void enableOnStartup() {
        orderChangingService.addOrderStateListener(orderStatesListener);
    }

    @Override
    public void disableOnStartup() {
        orderChangingService.removeOrderStateListener(orderStatesListener);
    }

}
