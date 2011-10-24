package com.qcadoo.mes.qualityControls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.orders.states.OrderStatesChangingService;
import com.qcadoo.plugin.api.Module;

@Component
public class QualityControlOnStartupService extends Module {

    @Autowired
    private OrderStatesChangingService orderStatesChangingService;

    @Autowired
    private QualityControlOrderStatesListener qualityControlOrderStatesListener;

    @Override
    public void enableOnStartup() {
        orderStatesChangingService.addOrderStateListener(qualityControlOrderStatesListener);
    }

    @Override
    public void disableOnStartup() {
        orderStatesChangingService.removeOrderStateListener(qualityControlOrderStatesListener);
    }

}
