package com.qcadoo.mes.genealogies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.orders.states.OrderStatesChangingService;
import com.qcadoo.plugin.api.Module;

@Component
public class GenealogyOnStartupService extends Module {

    @Autowired
    private OrderStatesChangingService orderStatesChangingService;

    @Autowired
    private GenealogyOrderStatesListener genealogyOrderStatesListener;

    @Override
    public void enableOnStartup() {
        orderStatesChangingService.addOrderStateListener(genealogyOrderStatesListener);
    }

    @Override
    public void disableOnStartup() {
        orderStatesChangingService.removeOrderStateListener(genealogyOrderStatesListener);
    }
}
